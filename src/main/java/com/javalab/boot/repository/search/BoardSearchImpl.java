package com.javalab.boot.repository.search;

import com.javalab.boot.dto.BoardImageDTO;
import com.javalab.boot.dto.BoardListAllDTO;
import com.javalab.boot.dto.BoardListReplyCountDTO;
import com.javalab.boot.entity.Board;
import com.javalab.boot.entity.QBoard;
import com.javalab.boot.entity.QReply;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 1. 이 클래스(BoardSearchImpl)는 Spring Data JPA와 Querydsl을
 *   함께 사용하여 Board 엔티티에 대한 사용자 정의 쿼리 메서드를 제공하는 역할
 * - QuerydslRepositorySupport는 JPQL 쿼리를 쉽게 작성하고
 *  실행할 수 있는 여러 도움 메서드를 제공하는 클래스.
 *  이를 통해 타입 안전한 쿼리를 작성 가능.
 */
@Log4j2
public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch {

    /**
     * QuerydslRepositorySupport는 엔티티 타입에 따른 Querydsl
     * 쿼리를 작성하게 도와주는데, 생성자에서 사용할 엔티티의
     * 타입(Board.class)을 지정합니다. 이로써 Board 엔티티에
     * 대한 Querydsl 쿼리 작성이 가능해집니다.
     */
    public BoardSearchImpl() {
        super(Board.class);
    }

    @Override
    public Page<Board> search1(Pageable pageable) {

        QBoard board = QBoard.board;

        // jpql 쿼리 객체 초기화
        // from 메서드를 사용하여 Board 엔티티에 대한 JPQL 쿼리를 초기화
        JPQLQuery<Board> query = from(board);

        // Querydsl에 제공될 조건문을 동적으로 생성할 수 있는 빌더 클래스
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // or 메소드 사용해서 조건 생성
        booleanBuilder.or(board.title.contains("tit")); // title like ...
        booleanBuilder.or(board.content.contains("con")); // content like ....

        // where 메소드를 통해서 위에서 만든 조건 설정
        query.where(booleanBuilder);
        // where 메소드에 조건 추가
        query.where(board.bno.gt(0L));
        //query.where(board.title.contains("1"));


        /*
         * 페이징
         * query : JPQL 쿼리객체
         * this.getQuerydsl() : 반환 타입이 Querydsl
         * applyPagination() : Querydsl에 Pageable 적용하여
         *  페이징 처리하는 역할.
         */
        this.getQuerydsl().applyPagination(pageable, query);

        // JPQLQuery 쿼리 실행
        List<Board> list = query.fetch();

        long count = query.fetchCount();
        return new PageImpl<>(list, pageable, count);

    } // end search1()

    @Override
    public Page<Board> searchAll(String[] types, String keyword, Pageable pageable) {

        QBoard board = QBoard.board;
        JPQLQuery<Board> query = from(board);

        if( (types != null && types.length > 0) && keyword != null ){ //검색 조건과 키워드가 있다면

            BooleanBuilder booleanBuilder = new BooleanBuilder(); // (

            for(String type: types){

                switch (type){
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            }//end for
            query.where(booleanBuilder);
        }//end if

        //bno > 0
        query.where(board.bno.gt(0L));

        // 불린 빌더 아닌 직접 where 메소드 사용 조건 생성
        // query.where(board.title.eq("1"));
        // 등록 일자 내림 차순 정렬
        // query.orderBy(board.regDate.desc());


        /*
          쿼리에 페이징 조건을 적용 이를 통해 주어진 Pageable 에 따라
          결과 페이징 처리함.
         */
        this.getQuerydsl().applyPagination(pageable, query);

        /*
          fetch 메서드를 사용하여 쿼리를 실행하고
          결과를 List<Board> 형태로 가져옴
         */
        List<Board> list = query.fetch();
        /*
          fetchCount 메서드 사용 하여 쿼리 결과의
          전체 레코드 갯수 가져옴.
         */
        long count = query.fetchCount();

        /*
          Spring Data의 Page 인터페이스의 구현체
          Page 인터페이스는 페이징 처리와 관련된 여러 메서드와
          정보를 제공. PageImpl<>을 사용 페이징 된 데이터
          관련된 정보를 캡슐화 함.
         */
        return new PageImpl<>(list, pageable, count);

    }

    /**
     * 게시물 목록과 그 게시물이 갖고 있는 댓글 갯수 조회
     * @param types
     * @param keyword
     * @param pageable
     * @return
     */
    @Override
    public Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types,
                                                             String keyword,
                                                             Pageable pageable) {
        // Q도메인 클래스(엔티티 객체의 메타정보 보관용)
        // querydsl 쿼리 객체 생성시 사용됨.(자동 생성)
        QBoard board = QBoard.board;
        QReply reply = QReply.reply;

        /**
         * [데이터를 받아와서 처리할 방법을 미리 설정해놓음]
         * Projections.constructor
         *  - 이 메서드는 BoardListReplyCountDTO 클래스의 생성자를 사용하여
         *    쿼리의 결과를 매핑한다. 쿼리의 결과 각 행을 BoardListReplyCountDTO의
         *    객체로 생성.
         *  - select()메소드를 통해서 데이터베이스로 부터 전달된 여러행의 결과를
         *    BoardListReplyCountDTO의 생성자를 통해서 해당 객체로 생성한다.
         *  - select()는 데이터베이스 처리 결과를 받아오고 받아온 행들을
         *    Projections.bean()을 통해서 DTO로 변환.
         *  - Projections.bean() : select()가 받아온 데이터를 Dto로 변환하는
         *    구체적인 역할.
         */
        JPQLQuery<BoardListReplyCountDTO> query = from(board)
                .leftJoin(reply).on(board.eq(reply.board))
                .groupBy(board)
                .select(Projections.bean(
                        BoardListReplyCountDTO.class,
                        board.bno,
                        board.title,
                        board.writer,
                        board.regDate,
                        reply.count().as("replyCount"))
                );

        if ((types != null && types.length > 0) && keyword != null) {
            BooleanBuilder booleanBuilder = new BooleanBuilder();

            for (String type : types) {
                switch (type) {
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            }
            // 쿼리 객체에 BooleanBuilder로 설정한 조건 지정
            query.where(booleanBuilder);
        }
        // 쿼리 객체에 and 조건 설정
        query.where(board.bno.gt(0L));
        // 현재 쿼리 객체에 페이징 조건 설정
        this.getQuerydsl().applyPagination(pageable, query);
        // 만들어진 쿼리 객체 실행해서 결과 받아옴
        List<BoardListReplyCountDTO> list = query.fetch();
        // 조건과 일치하는 결과 행수 조회
        long count = query.fetchCount();
        // 데이터베이스에서 받아온 데이터와 페이징 조건, 결과 행수로
        // Page객체 생성해서 반환
        return new PageImpl<>(list, pageable, count);
    }

    // 게시물 목록과 그 게시물이 갖고 있는 게시물 댓글 List함께 조회
    @Override
    public Page<BoardListAllDTO> searchWithAll(String[] types,
                                               String keyword,
                                               Pageable pageable) {

        // 1 단계
        /*
        // querydsl 객체지향 쿼리 객체 생성을 위한 Qtype domain
        QBoard qBoard = QBoard.board;
        QReply qReply = QReply.reply;

        // 쿼리 객체 초기화(드라이빙 테이블 선정)
        JPQLQuery<Board> queryObj = from(qBoard);
        //left join(댓글의 게시글 번호와 게시글의 번호가 같아야)
        queryObj.leftJoin(qReply).on(qReply.board.eq(qBoard));

        // 페이지네이션 적용
        this.getQuerydsl().applyPagination(pageable, queryObj); //paging
        // 쿼리 실행
        List<Board> boardList = queryObj.fetch();

        boardList.forEach(board1 ->{
            log.info(board1.getBno());
            log.info(board1.getImageSet());
            log.info("------------------");
        });
        return null;
        */

        // 2단계 querydsl 객체지향 쿼리 객체 생성을 위한 Qtype domain
        QBoard qBoard = QBoard.board;
        QReply qReply = QReply.reply;

        // 쿼리 객체 초기화(드라이빙 테이블 선정)
        JPQLQuery<Board> queryObj = from(qBoard);

        //left join(댓글의 게시글 번호와 게시글의 번호가 같아야)
        queryObj.leftJoin(qReply).on(qReply.board.eq(qBoard));

        if( (types != null && types.length > 0) && keyword != null ){
            BooleanBuilder booleanBuilder = new BooleanBuilder(); // (
            for(String type: types){
                switch (type){
                    case "t":
                        booleanBuilder.or(qBoard.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(qBoard.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(qBoard.writer.contains(keyword));
                        break;
                }
            }//end for
            // 쿼리 객체에 or조건 설정
            queryObj.where(booleanBuilder);
        }
        // 게시물당 이미지갯수 count()해야 해서 그룹바이 설정
        queryObj.groupBy(qBoard);

        // 페이지네이션 적용
        this.getQuerydsl().applyPagination(pageable, queryObj); //paging

        // 쿼리를 문자열로 변환하여 콘솔에 출력
        log.info("쿼리객체-1 queryObj 쿼리문장 : " + queryObj.toString());

        /**
         * Tuple 사용 이유
         * - 쿼리 결과를 다양한 타입을 묶어서 반환 받을 수 있다.
         * [
         *     [Board(bno=100, title=Title..100, content=Content..100, writer=writer..100), 2],
         *     [Board(bno=99, title=Title..99, content=Content..99, writer=writer..99), 2]
         * ]
         * - 다음과 같이 다양한 타입(클래스) 형태로 결과를 받을 수 있어서 이걸 가공해서
         *   사용하기 편리함.
         * [
         *     [Board객체1, 1, Reply객체1],
         *     [Board객체2, 2, Reply객체2],
         *     [Board객체3, 3, Reply객체3],
         * ]
         */
        // 처음의 쿼리객체<Board> -> Turple Type변환
        JPQLQuery<Tuple> tupleJPQLQuery = queryObj.select(qBoard, qReply.countDistinct());
        log.info("쿼리객체-2 tupleJPQLQuery 쿼리문장 : " + tupleJPQLQuery);

        List<Tuple> tupleList = tupleJPQLQuery.fetch();
        log.info("쿼리 결과-3 List<Tuple> : " + tupleList);


        // [반복]게시물 엔티티 갯수만큼(게시물10개면 10회 반복)
        List<BoardListAllDTO> dtoList = tupleList.stream().map(tuple -> {

            // 게시물 엔티티
            Board board = tuple.get(0, Board.class);    //동일
            long replyCount = tuple.get(1,Long.class);

            // BoardListAllDTO : Board + count + imgSet 모두 포함됨.
            // 게시물 엔티티 -> 게시물 DTO 변환(아직까지는 Board에 img 파일 없음, Lazy)
            BoardListAllDTO boardDto = BoardListAllDTO.builder()
                    .bno(board.getBno())
                    .title(board.getTitle())
                    .writer(board.getWriter())
                    .regDate(board.getRegDate())
                    .replyCount(replyCount)
                    .build();

            // [이미지 작업]
            // 1. board 연관된 BoardImage들 조회.
            // 2. 이전까지는 Board에 BoardImage정보가 없었다. 그러나
            //    getImageSet()를 호출하게 되면 그때 BoardImage에 가서 조회함.
            //    board.getImageSet() : bno로 image 파일 조회.
            // 3. BoardImage를 BoardImageDTO 변환.
            // 4. @BatchSize() 조건 없으면 N + 1회 쿼리 실행, 퍼포먼스 떨어짐
            List<BoardImageDTO> imageDTOS = board.getImageSet().stream().sorted()
                    .map(boardImage -> BoardImageDTO.builder()
                            .uuid(boardImage.getUuid())
                            .fileName(boardImage.getFileName())
                            .ord(boardImage.getOrd())
                            .build()
                    ).collect(Collectors.toList());

            // 변환된 이미지DTO -> BoardListAllDTO에 세팅
            boardDto.setBoardImages(imageDTOS);
            // BoardListAllDTO 1개 완성

            return boardDto;

        }).collect(Collectors.toList());

        long totalCount = queryObj.fetchCount();

        return new PageImpl<>(dtoList, pageable, totalCount);
    }



}
