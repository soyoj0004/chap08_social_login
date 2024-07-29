package com.javalab.boot.controller;

import com.javalab.boot.dto.BoardDTO;
import com.javalab.boot.dto.PageRequestDTO;
import com.javalab.boot.dto.PageResponseDTO;
import com.javalab.boot.service.BoardService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Log4j2
public class BoardControllerTest {

    @Autowired
    private BoardService boardService;

    //@Autowired
    //private BoardRepository boardRepository;

    @Disabled
    @Test
    public void testListBoard() {
        // 페이지 요청 DTO 생성 (예: 첫 페이지의 10개 게시물)
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .type("t") // title 컬럼만 검색
                .keyword("1")// 검색어
                .page(1)
                .size(10)
                .build();

        // 서비스 레이어의 list 메소드 호출
        PageResponseDTO<BoardDTO> responseDTO = boardService.list(pageRequestDTO);

        // 반환된 결과 검증
        List<BoardDTO> dtoList = responseDTO.getDtoList();
        dtoList.stream().forEach(b->log.info(b.toString()));

        assertThat(dtoList).isNotEmpty(); // 목록이 비어있지 않아야 함

        // 반환된 페이지 정보 검증 (예: 실제로 10개 이하의 게시물이 반환되었는지)
        assertThat(dtoList.size()).isLessThanOrEqualTo(10);
    }

    // 게시물 등록 단위테스트
    @Disabled
    @Test
    public void testRegisterPost() {
        BoardDTO boardDTO = BoardDTO.builder()
                .title("새로 추가한 타이틀2")
                .content("새로 추가된 컨텐트2")
                .writer("새로 추가한 작성자2")
                .build();

        Long registeredId = boardService.register(boardDTO);

        // 이 부분은 데이터베이스에서 실제로 데이터를 확인하기 위한 코드입니다.
        BoardDTO resultDTO = boardService.readOne(registeredId);

        assertEquals(boardDTO.getTitle(), resultDTO.getTitle());
        assertEquals(boardDTO.getContent(), resultDTO.getContent());
        assertEquals(boardDTO.getWriter(), resultDTO.getWriter());

//        assertThat(resultDTO.getTitle()).isEqualTo(boardDTO.getTitle());
//        assertThat(resultDTO.getContent()).isEqualTo(boardDTO.getContent());
//        assertThat(resultDTO.getWriter()).isEqualTo(boardDTO.getWriter());

    }

    @Test
    @Disabled
    public void testModifyBoard() {
        // 수정할 게시물 번호
        Long bno = 201L; // 실제 있는 번호

        // 수정할 내용을 가진 DTO 객체 생성
        BoardDTO boardDTO = BoardDTO.builder()
                .bno(bno)
                .title("수정된 타이틀")
                .content("수정된 내용")
                .build();

        // 게시물 수정
        boardService.modify(boardDTO);

        // 수정된 내용 검증
        BoardDTO modifiedDTO = boardService.readOne(bno);
        assertEquals(modifiedDTO.getTitle(), boardDTO.getTitle());
    }

    @Test
    @Disabled
    public void testRemoveBoard() {
        // 수정할 게시물 번호
        Long bno = 201L; // 실제 있는 번호
        boardService.remove(bno);
    }

}
