package com.javalab.boot.repository;

import com.javalab.boot.entity.Board;
import com.javalab.boot.repository.search.BoardSearch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Board 레파지토리 클래스
 *  - BoardSearch 인터페이스 상속 : BoardSearch 인터페이스의
 *   구현체(Impl)가 갖고 있는 모든 메소드에 접근 가능해짐.
 *   Spring Data JPA에서 JpaRepository를 확장하는 인터페이스는
 *   자동으로 빈으로 등록
 */
public interface BoardRepository extends JpaRepository<Board, Long>, BoardSearch {

    @Query(value="select now()", nativeQuery = true)
    String getTime();

    /**
     * @EntityGraph :
     *  - Board와 BoardImage가 지연로딩 관계이지만
     *  필요에 따라서 연관된 BoardImage를 즉시 로딩되도록 해줌.
     * attributePaths :
     *  - 게시물 조회시 즉시 조회할 엔티티(속성)를 명시.
     * @param bno
     * @return
     */
    @EntityGraph(attributePaths = {"imageSet"})
    @Query("select b from Board b where b.bno =:bno")
    Optional<Board> findByIdWithImages(@Param("bno") Long bno);
}
