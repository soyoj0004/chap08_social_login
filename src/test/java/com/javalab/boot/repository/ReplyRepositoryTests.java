package com.javalab.boot.repository;

import com.javalab.boot.entity.Board;
import com.javalab.boot.entity.Reply;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SpringBootTest
@Log4j2
public class ReplyRepositoryTests {

    @Autowired
    private ReplyRepository replyRepository;

    /**
     * 댓글 등록 메소드(원하는 만큼 생성)
     * - 1, 2번 게시물의 댓글등록
     * - 100, 99번 게시물의 댓글도 등록해야 테스트하기 좋음.
     */
    @Disabled
    @Test
    public void testInsert() {
        //실제 DB에 있는 bno
        // 1단계
        /*
        Long bno  = 1L;
        Board board = Board.builder().bno(bno).build();
        Reply reply = Reply.builder()
                .board(board)
                .replyText("댓글.....")
                .replyer("replyer1")
                .build();
        replyRepository.save(reply);
        */
        // 2단계
        Long bno = 99L; // 댓글을 달 게시물 번호를 여기에 입력하세요.
        Board board = Board.builder().bno(bno).build();

        int numberOfReplies = 2; // 원하는 댓글의 수를 여기에 설정하세요.

        for (int i = 0; i < numberOfReplies; i++) {
            Reply reply = Reply.builder()
                    .board(board)
                    .replyText("댓글" + i + ".....") // 댓글 텍스트에 반복 횟수를 추가하여 고유하게 만듭니다.
                    .replyer("replyer" + i) // 댓글 작성자에도 반복 횟수를 추가하여 고유하게 만듭니다.
                    .build();

            replyRepository.save(reply);
        }
    }

    //@Transactional
    @Test
    public void testBoardReplies() {

        Long bno = 103L; // 실제 DB 게시물 번호(reply table)
        Pageable pageable = PageRequest.of(0,10, Sort.by("rno").descending());
        Page<Reply> result = replyRepository.listOfBoard(bno, pageable);
        result.getContent().forEach(reply -> {
            log.info(reply);
        });
    }

}
