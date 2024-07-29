package com.javalab.boot.controller;


import com.javalab.boot.dto.BoardDTO;
import com.javalab.boot.dto.BoardListAllDTO;
import com.javalab.boot.dto.PageRequestDTO;
import com.javalab.boot.dto.PageResponseDTO;
import com.javalab.boot.service.BoardService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 게시판 컨트롤러
 * @Controller : 스프링 빈으로 등록되는 클래스.
 *  - 화면에서 전달된 파라미터 수집 역할.
 *  - 서비스 레이어로 파라미터 전달.
 *  - 서비스 레이어 에서 결과 반환 받고
 *    뷰 이름을 리턴. 뷰 리졸버는 반환된 뷰 이름으로
 *    실제로 처리를 담당할(데이터를 화면에 보여줄 페이지를
 *    구성하는) 페이지를 찾아서 데이터 전달.
 *  -디스패처 서블릿은 해당 타임리프 페이지에게 데이터전달
 * @RequestMapping("/board") : 컨트롤러 차원의 Url 매핑 문자열
 *  - 모든 메소드에 공통적으로 적용되는 부분을 뽑아 올림.
 */
@Controller
@RequestMapping("/board")
@Log4j2
public class BoardController {

    // 의존성 주입
    @Autowired
    private BoardService boardService;

    @Value("${com.javalab.boot.upload.path}")
    private String uploadPath;

    /**
     * 공통메소드
     *  - 이 컨트롤러의 모든 메소드가 호출되면 그 전에 이 메소드가 호출됨.
     *  - 이 메소드의 반환값은 자동으로 model에 저장됨.
     *  - model에 저장될 때 "roles"라는 이름으로 저장됨.
     */
    @ModelAttribute("roles")
    public Map<String, String> getRoles() {
        Map<String, String> roles = new HashMap<>();
        roles.put("manager", "Manager");
        roles.put("user", "User");
        return roles;
    }

    /*
      게시물 목록 조회 처리 핸들러(메소드)
     */
    @GetMapping("/list")
    public String list(PageRequestDTO pageRequestDTO, Model model){
        PageResponseDTO<BoardListAllDTO> responseDTO =
                boardService.listWithAll(pageRequestDTO);

        log.info(responseDTO);
        model.addAttribute("responseDTO", responseDTO);
        return "board/list";
    }

    // 회원등록폼
    @GetMapping("/register")
    public String registerGet(Model model) {
        log.info("게시물 등록폼 오픈 메소드");
        model.addAttribute("boardDTO", new BoardDTO());
        return "board/register";
        //return "board/register_bootstrap";
    }

    /**
     * 회원 등록 처리 메소드
     *  - @Valid BoardDTO boardDTO : 화면에서 넘어온 값을 바인딩하기 전에 검증.
     *  - bindingResult : 검증 결과 저장 변수.
     *  - redirectAttributes : 처리후 이동해간 페이지에서 보여줄 값 전달용
     */
    @PostMapping("/register")
    public String registerPost(@Valid BoardDTO boardDTO,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model){

        if(bindingResult.hasErrors()) {
            log.info("등록 화면 오류 있음");
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "board/register"; // board/register.html 뷰로 이동
        }
        // 등록처리
        Long bno  = boardService.register(boardDTO);
        // 등록된 번호를 이동해갈 페이지에서 사용하도록 저장
        redirectAttributes.addFlashAttribute("result", bno);

        return "redirect:/board/list"; // 새로운 요청(/board/list 요청)
    }

    /**
     * 게시물 상세보기(내용보기)
     */
    @GetMapping("/read")
    public String read(@RequestParam("bno") Long bno, PageRequestDTO pageRequestDTO, Model model){
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info(boardDTO);
        model.addAttribute("board", boardDTO);
        return "board/read";
    }

    /**
     * 게시물 수정 화면 오픈 메소드
     * 파라미터 전달방식 두 가지
     *  1.첫째, 일반적인 형태
     *   - @GetMapping("/modify")
     *   - public void modifyGet(@RequestParam("bno") Long bno
     *  2. 둘째, PathVariable 형태
     *   - @GetMapping("/modify/{bno}")
     *   - public void modifyGet(@PathVariable("bno") Long bno
     */
    //@GetMapping("/modify/{bno}")
    //public String modifyGet(@PathVariable("bno") Long bno, PageRequestDTO pageRequestDTO, Model model){
    @GetMapping("/modify")
    public String modifyGet(@RequestParam("bno") Long bno, PageRequestDTO pageRequestDTO, Model model){
        BoardDTO boardDTO = boardService.readOne(bno);
        model.addAttribute("board", boardDTO);
        return "board/modify";
    }

    /**
     * 게시물 수정 처리 메소드
     * @param pageRequestDTO : 페이징 정보
     * @param boardDTO : 화면에서 입력한 게시물 정보
     *  - 만약 오류가 있어서 다시 화면으로 가게되면 그대로 전달됨.
     *  - @ModelAttribute("board") : 이동해간 페이지에서 다른 이름으로 꺼내쓰고 싶을떄 이름 지정
     * @param bindingResult : 게시물 정보 검증 결과값
     * @param redirectAttributes : 이동해갈 페이지로 전달할 값저장 역할
     */
    @PostMapping("/modify")
    public String modifyPost(PageRequestDTO pageRequestDTO,
                             @Valid @ModelAttribute("board") BoardDTO boardDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model){

        // 오류가 있으면 처리하는 로직
        if(bindingResult.hasErrors()) {
            log.info("게시물 수정 화면 오류 있음");
            // 화면에서 넘어온 데이터 다시 화면으로 보냄
            //model.addAttribute("board", boardDTO);
            // 오류 정보도 저장해서 보냄
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "board/modify"; // board/modify.html 뷰로 이동
        }

        // 오류가 없어서 저장 작업
        boardService.modify(boardDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("bno", boardDTO.getBno());
        return "redirect:/board/read"; // 저장후 게시물 상세보기 화면으로 이동
    }

    @PostMapping("/remove")
    public String remove(BoardDTO boardDTO, RedirectAttributes redirectAttributes) {

        Long bno  = boardDTO.getBno();
        log.info("remove post.. " + bno);

        boardService.remove(bno);

        // 게시물이 삭제되었다면 첨부 파일 삭제
        log.info("boardDTO.getFileNames() : " + boardDTO.getFileNames());

        List<String> fileNames = boardDTO.getFileNames();

        if(fileNames != null && fileNames.size() > 0){
            removeFiles(fileNames);
        }

        redirectAttributes.addFlashAttribute("result", "removed");

        return "redirect:/board/list";
    }

    // 파일 시스템에서 파일 삭제
    public void removeFiles(List<String> files){
        log.info("여기는 파일 삭제 메소드 removeFiles");
        for (String fileName: files) {

            Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
            String resourceName = resource.getFilename();

            try {
                String contentType = Files.probeContentType(resource.getFile().toPath());
                resource.getFile().delete();
                log.info("여기는 파일 삭제 메소드 removeFiles -2 ");

                //섬네일이 존재한다면
                if (contentType.startsWith("image")) {
                    File thumbnailFile = new File(uploadPath + File.separator + "s_" + fileName);
                    thumbnailFile.delete();
                    log.info("여기는 파일 삭제 메소드 removeFiles -3 ");
                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }

        }//end for
    }

    /**
     * 스웨거 테스트용 메소드로 게시물 목록 조회
     * @param pageRequestDTO
     * @return
     */
    @GetMapping("/boardlist")
    public ResponseEntity<PageResponseDTO<BoardDTO>> boardList(PageRequestDTO pageRequestDTO){
        PageResponseDTO<BoardDTO> responseDTO = boardService.list(pageRequestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    /**
     * 스웨거 테스트용 메소드로 게시물 한개 조회
     * @param bno
     * @return
     */
    @GetMapping("/boardread/{bno}")
    public ResponseEntity<BoardDTO> boardRead(@PathVariable Long bno){
        BoardDTO boardDTO = boardService.readOne(bno);
        return new ResponseEntity<>(boardDTO, HttpStatus.OK);
    }

}
