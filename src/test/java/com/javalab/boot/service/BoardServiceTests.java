package com.javalab.boot.service;

import com.javalab.boot.dto.*;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service Layer 단위 테스트 케이스
 */
@SpringBootTest
@Log4j2
public class BoardServiceTests {

    @Autowired
    private BoardService boardService;

    @Disabled
    @Test
    public void testRegister() {
        /*
          [boardService 참조 변수]
          BoardService 인터페이스의 구현체인 BoardServiceImpl
          클래스의 객체가 아니라 전혀 다른 proxy 객체가 주입됨.
          Spring의 AOP(Aspect-Oriented Programming)와 트랜잭션
          관리 메커니즘 때문. Spring은 서비스 레이어의 메서드에
          대한 트랜잭션 관리를 제공하기 위해, 해당 서비스의 구현체
          대신 프록시 객체를 생성하여 사용함. 이 프록시 객체는 실제
          BoardServiceImpl의 메서드 호출 전후에 트랜잭션 관련 로직을
          추가하여 실행함.
         */
        log.info("testRegister메소드 : " + boardService.getClass().getName());

        // BoardDTO 객체 생성
        BoardDTO boardDTO = BoardDTO.builder()
                .title("Sample Title...")
                .content("Sample Content...")
                .writer("user00")
                .build();

        Long bno = boardService.register(boardDTO);

        log.info("저장한 엔티티의 bno: " + bno);

    }

    // 게시물 한개 조회(상세보기)
    @Disabled
    @Test
    public void testReadOne() {
        // 존재하는 bno 값을 사용하여 테스트
        Long existingBno = 1002L;  // 실제 데이터베이스에 존재하는 값
        BoardDTO boardDTO = boardService.readOne(existingBno);
        log.info(boardDTO);

        Assertions.assertNotNull(boardDTO);  // boardDTO가 null이 아닌지 확인
        Assertions.assertEquals(existingBno, boardDTO.getBno());  // 반환된 DTO의 bno 값이 기대하는 값과 일치하는지 확인
    }

    // 수정
    @Disabled
    @Test
    public void testModify() {
        //변경에 필요한 데이터만
        BoardDTO boardDTO = BoardDTO.builder()
                .bno(101L)
                .title("수정됨....101")
                .content("수정된 content 101")
                .build();
        boardDTO.setFileNames(Arrays.asList(UUID.randomUUID() + "_zzz.jpg"));
        boardService.modify(boardDTO);
    }

    // 삭제 단위 테스트
    @Disabled
    @Test
    public void testRemoveAll(){
        boardService.remove(1L);
    }

    /**
     * 게시물 목록 조회
     */
    @Disabled
    @Test
    public void testList() {
        // 화면에서 넘어온 정보라고 가정
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .type("tcw") // title, content, writer
                .keyword("1")// 검색어
                .page(1) // 요청 페이지(1을 전달하면 받는쪽에서 -1해서 0이됨, 즉 첫페이지)
                .size(10) // 페이지당 게시물수
                .build();

        // 조회 결과를 PageResponseDTO에 저장.
        // [Ctrl + T] : 메소드 선언부(인터페이스)가 아닌 구현부(Impl)로 이동.
        PageResponseDTO<BoardDTO> responseDTO =
                boardService.list(pageRequestDTO);

        List<BoardDTO> boardDTOList = responseDTO.getDtoList();
        boardDTOList.stream().forEach(b->log.info(b.toString()));
        //log.info(responseDTO);
    }

    @Disabled
    @Test
    public void testRegisterWithImages() {

        log.info(boardService.getClass().getName());

        BoardDTO boardDTO = BoardDTO.builder()
                .title("File...Sample Title...")
                .content("Sample Content...")
                .writer("user00")
                .build();

        boardDTO.setFileNames(
                Arrays.asList(
                        UUID.randomUUID()+"_aaa.jpg",
                        UUID.randomUUID()+"_bbb.jpg",
                        UUID.randomUUID()+"_ccc.jpg"
                ));

        Long bno = boardService.register(boardDTO);

        log.info("bno: " + bno);
    }

    @Test
    @Disabled
    public void testReadAll() {
        Long bno = 101L; // DB에 있는 번호
        BoardDTO boardDTO = boardService.readOne(bno);
        log.info(boardDTO);
        for (String fileName : boardDTO.getFileNames()) {
            log.info(fileName);
        }//end for
    }


    @Disabled
    @Test
    public void testListWithAll() {
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                                        .page(1)
                                        .size(10)
                                        .build();

        PageResponseDTO<BoardListAllDTO> responseDTO =
                boardService.listWithAll(pageRequestDTO);

        List<BoardListAllDTO> dtoList = responseDTO.getDtoList();

        dtoList.forEach(boardListAllDTO -> {
            log.info(boardListAllDTO.getBno()+":"+boardListAllDTO.getTitle());

            if(boardListAllDTO.getBoardImages() != null) {
                for (BoardImageDTO boardImage : boardListAllDTO.getBoardImages()) {
                    log.info(boardImage);
                }
            }
            log.info("-------------------------------");
        });
    }

}
