package com.javalab.boot.repository;


import com.javalab.boot.dto.BoardListAllDTO;
import com.javalab.boot.dto.BoardListReplyCountDTO;
import com.javalab.boot.entity.Board;
import com.javalab.boot.entity.BoardImage;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootTest
@Log4j2
public class BoardRepositoryTests {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ReplyRepository replyRepository;


    @Disabled
    @Test
    public void testInsert() {
        IntStream.rangeClosed(1,100).forEach(i -> {
            Board board = Board.builder()
                    .title("title..." +i)
                    .content("content..." + i)
                    .writer("user"+ (i % 10))
                    .build();

            Board result = boardRepository.save(board);
            log.info("BNO: " + result.getBno());
        });
    }

    @Disabled
    @Test
    public void testSelect() {
        Long bno = 1001L;

        Optional<Board> result = boardRepository.findById(bno);
        if(result.isPresent()){
            Board board = result.get();
            log.info(board);
        }else{
            log.info("해당 게시물이 존재하지 않습니다.");
        }
    }

    @Disabled
    @Test
    public void testUpdate() {

        Long bno = 100L;

        Optional<Board> result = boardRepository.findById(bno);

        Board board = result.orElseThrow();

        board.change("업데이트..title 100", "업데이트 content 100");

        boardRepository.save(board);

    }

    @Disabled
    @Test
    public void testDelete() {
        Long bno = 1L;

        boardRepository.deleteById(bno);
    }

    @Disabled
    @Test
    public void testPaging() {

        // 1. 페이징을 위한 조건 생성(0-첫번째 페이지, 10-10개 게시물, 정렬컬럼, 정렬방식
        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());

        Page<Board> result = boardRepository.findAll(pageable);

        // 페이징 관련 여러 정보를 담고 있음.
        log.info("total count: "+result.getTotalElements());
        log.info( "total pages:" +result.getTotalPages());
        log.info("page number: "+result.getNumber());
        log.info("page size: "+result.getSize());

        List<Board> todoList = result.getContent();

        todoList.forEach(board -> log.info(board));
    }


    /**
     * QBoard 오류시 다시 시도
     * 그래도 안되면 앱 ReBuild
     */
    @Disabled
    @Test
    public void testSearch1() {

        // 페이징을 위한 객체 생성
        Pageable pageable = PageRequest.of(1,10, Sort.by("bno").descending());

        // 페이징 객체를 search1() 메소드에 전달
        Page<Board> result  = boardRepository.search1(pageable);

        result.getContent().forEach(b->log.info(b.toString()));

    }

    @Disabled
    @Test
    public void testSearchAll() {

        String[] types = {"t","c","w"};

        String keyword = "1";

        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());

        Page<Board> result = boardRepository.searchAll(types, keyword, pageable );

        result.getContent().forEach(b->log.info(b.toString()));
    }
    @Disabled
    @Test
    @Transactional
    public void testSearchAll2() {

        // title, content, writer 모든 조건
        String[] types = {"t","c","w"};

        String keyword = "1";

        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());

        Page<Board> result = boardRepository.searchAll(types, keyword, pageable );

        //total pages
        log.info(result.getTotalPages());

        //pag size
        log.info(result.getSize());

        //pageNumber
        log.info(result.getNumber());

        //prev next
        log.info(result.hasPrevious() +": " + result.hasNext());

        //result.getContent().forEach(board -> log.info(board));

        // 다음 생략하면 Lazy Loading됨.
        result.getContent().forEach(board -> {
                if(board.getImageSet() != null)
                log.info(board.getBno() + "번의 첨부 이미지 갯수는 : " + board.getImageSet().size());
        });
    }

    @Disabled
    @Test
    public void testSearchReplyCount() {

        String[] types = {"t","c","w"};

        String keyword = "1";

        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());

        Page<BoardListReplyCountDTO> result = boardRepository.searchWithReplyCount(types, keyword, pageable );

        //total pages
        log.info(result.getTotalPages());
        //pag size
        log.info(result.getSize());
        //pageNumber
        log.info(result.getNumber());
        //prev next
        log.info(result.hasPrevious() +": " + result.hasNext());

        result.getContent().forEach(board -> log.info(board));
    }

    /**
     * [게시물과 게시물에 달린 이미지 생성 메소드]
     * 기존에 있는 게시물 번호를 넣으면 그 게시물은 수정되고
     * 이미지는 새로 만들어짐.
     * drop table reply;
     * drop table board_image ;
     * drop table board;
     */
    @Disabled
    @Test
    public void testInsertWithImages() {
        // 게시물 엔티티 생성
        Board board = Board.builder()
                .bno(98L)
                .title("Image Test")
                .content("첨부파일 테스트")
                .writer("tester")
                .build();
        // 게시물 이미지 엔티티 3개씩 생성
        for (int i = 0; i < 3; i++) {
            board.addImage(UUID.randomUUID().toString(), "file" + i + ".jpg");
        }//end for
        // 영속화
        boardRepository.save(board);
    }

    @Test
    @Transactional
    @Disabled
    public void testReadWithImages1() {

        //반드시 존재하는 bno로 확인
        Optional<Board> result = boardRepository.findById(101L);

        Board board = result.orElseThrow();

        log.info("게시물 조회 board : " + board);
        log.info("--------------------");

        // 이때까지는 게시물 첨부 이미지를 조회하지 않는다.
        // 다음과 같이 board.getImageSet()을 요청했을 때 비로소 Lazy하게 쿼리하게 된다.
        log.info("board.getImageSet() : " + board.getImageSet());
    }

    /**
     * Board 와  BoardImage는 지연로딩 관계이지만
     * @EntityGraph를 이용해서 즉시 로딩되는 메소드를 만들어서
     * 실행하는 테스트
     */
    @Disabled
    @Test
    public void testReadWithImages2() {

        // DB에 존재하는 bno로 테스트
        // 한번에 Board와 BoardImage Left Join 실행
        Optional<Board> result = boardRepository.findByIdWithImages(101L);

        Board board = result.orElseThrow();

        log.info(board);
        log.info("--------------------");
        for (BoardImage boardImage : board.getImageSet()) {
            log.info(boardImage);
        }
    }

    /**
     * 게시물 엔티티에서 게시물 첨부 이미지 엔티티 수정
     *  - Board의 imageSet의 옵션을 orphanRemoval = true
     *    즉, 고아 객체 삭제
     *  - orphanRemoval = true 조건이 없으면 영속 영역에 있는
     *    BoardImage들이 고아 상태로 남아있게 되고 데이터베이스에도
     *    board_bno가 null인 상태로 계속 유지가 된다.
     *  - orphanRemoval = true 조건이 있으면 영속 영역에 있는
     *    BoardImage들이 고아 상태가 되면 자동으로 데이터베이스에도
     *    반영한다.
     *
     *  delete from board_image where board_bno is null;
     */
    @Disabled
    @Transactional
    @Commit
    @Test
    public void testModifyImages() {

        // 조회 -> 영속성 컨텍스트에 Board 엔티티가 영속화됨.
        Optional<Board> result = boardRepository.findByIdWithImages(101L);

        Board board = result.orElseThrow();

        //기존의 첨부파일들은 삭제
        board.clearImages();

        //새로운 첨부파일들
        for (int i = 0; i < 2; i++) {
            board.addImage(UUID.randomUUID().toString(), "updatefile"+i+".jpg");
        }

        boardRepository.save(board);

    }

    /**
     * 삭제 테스트
     */
    @Disabled
    @Test
    @Transactional
    @Commit
    public void testRemoveAll() {

        Long bno = 101L;
        replyRepository.deleteByBoard_Bno(bno);
        boardRepository.deleteById(bno);

    }

    /**
     * @Batch 테스트
     * 기존 테이블 모두 삭제후 작업
     * drop table reply;
     * drop table board_image ;
     * drop table board;
     */
    @Disabled
    @Test
    public void testInsertAll() {

        for (int i = 1; i <= 100; i++) {

            Board board  = Board.builder()
                    .title("Title.."+i)
                    .content("Content.." + i)
                    .writer("writer.." + i)
                    .build();

            for (int j = 0; j < 3; j++) {

                if(i % 5 == 0){
                    continue;
                }
                board.addImage(UUID.randomUUID().toString(),i+"file"+j+".jpg");
            }
            boardRepository.save(board);

        }//end for
    }

    /**
     * @Batch 테스트 조회
     *  - 디비에 댓글, BoardImage 존재 유무 확인후 테스트
     *
    */
    @Disabled
    @Transactional
    @Test
    public void testSearchImageReplyCount() {

        // 공통조건
        Pageable pageable = PageRequest.of(0,10,Sort.by("bno").descending());

        // 1단계
        //boardRepository.searchWithAll(null, null, pageable);

        // 2단계
        Page<BoardListAllDTO> result = boardRepository.searchWithAll(null,null,pageable);
        log.info("---------------------------");
        log.info("getTotalElements : " + result.getTotalElements());
        // Board안에 BoardImageDTO를 확인해 볼 수 있다.
        result.getContent().forEach(boardListAllDTO -> log.info(boardListAllDTO));

    }
}