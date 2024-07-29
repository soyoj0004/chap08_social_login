package com.javalab.boot.repository;

import com.javalab.boot.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    // JPQL Query, Reply table 에서 특정 게시물에 소속된 댓글 조회
    @Query("select r from Reply r where r.board.bno = :bno")
    Page<Reply> listOfBoard(@Param("bno")Long bno, Pageable pageable);

    // 추가
    void deleteByBoard_Bno(Long bno);
}
