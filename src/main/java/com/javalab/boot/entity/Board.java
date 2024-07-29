package com.javalab.boot.entity;

import com.javalab.boot.dto.BoardDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Board 엔티티
 * - @BatchSize(size = 20) : N + 1 문제 완화.
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "imageSet")
public class Board extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;
    @Column(length = 500, nullable = false) //컬럼의 길이와 null허용여부
    private String title;
    @Column(length = 2000, nullable = false)
    private String content;
    @Column(length = 50, nullable = false)
    private String writer;
    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }
    /**
     * @OneToMany : 연관관계 매핑, 일대다관계
     * cascade = {CascadeType.ALL}
     *  - 부모 엔티티의 변화가 자식 엔티티에 모두 반영
     * fetch = FetchType.LAZY
     *  - 현재 엔티티와 연관된 엔티티는 최대한 필요한 시점에 로딩(쿼리)
     *  - @OneToMany는 기본이 Lazy loding 방식
     * @BatchSize(size = 20) : 쿼리문에 in() 생성됨.
     *  - 관련된 BoardImage를 로딩할때 부모쪽에 리스트된 10개를
     *    in(board1, board2...board10) 조건으로 주어서 한꺼번에
     *    BoardImage를 조회해옴.
     *  - 이 조건 누락시 10개의 게시물 마다 각각 이미지 조회 쿼리 나감.(10회)
     *  - N(10회) + 1회 총11번 쿼리하는 것을 단 2회로 줄여줌.
     * orphanRemoval = true
     *  - 부모에 대한 관계가 끊어진 자식들을 삭제하는 기능.
     *  - 이기능 미적용시 부모에 대한 참조가 null인 상태
     */
    @OneToMany(mappedBy = "board",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
            ,orphanRemoval = true
    ) // BoardImage의 board 변수
    @Builder.Default
    @BatchSize(size = 20) // 이 조건 누락시 10개의 게시물마다 이미지 조회 쿼리 나감.(완전 비효율적)
    private Set<BoardImage> imageSet = new HashSet<>();

    // 첨부 이미지 추가
    public void addImage(String uuid, String fileName) {
        BoardImage boardImage = BoardImage.builder()
                .uuid(uuid)
                .fileName(fileName)
                .board(this)
                .ord(imageSet.size())
                .build();
        imageSet.add(boardImage);
    }

    // 첨부 이미지 엔티티 삭제
    public void clearImages() {
        imageSet.forEach(boardImage -> boardImage.changeBoard(null));
        this.imageSet.clear();
    }

    // 엔티티 -> DTO 변환
    public BoardDTO entityToDto() {
        BoardDTO boardDTO = BoardDTO.builder()
                .bno(this.getBno())
                .title(this.getTitle())
                .content(this.getContent())
                .writer(this.getWriter())
                .regDate(this.getRegDate())
                .modDate(this.getModDate())
                .build();

        // this.getImageSet() 하게 되면 비로서 현재 Board 객체와 연결된
        // BoardImage 들이 로딩된다.
        List<String> fileNames =
                this.getImageSet().stream().sorted().map(boardImage ->
                        boardImage.getUuid() + "_"
                                + boardImage.getFileName()
                ).collect(Collectors.toList());

        boardDTO.setFileNames(fileNames);

        return boardDTO;
    }

}