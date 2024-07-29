package com.javalab.boot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "board")
public class BoardImage implements Comparable<BoardImage> {

    @Id
    private String uuid;

    private String fileName;

    private int ord;

    /*
      Board와 연관관계 매핑, 다대일 관계
      부모 객체의 참조가 들어온다.
      이 조건으로 실제 테이블에는 board_bno 외래키가 생성됨.
     */
    @ManyToOne
    private Board board;

    // 주로 정렬이 필요한 데이터 구조에서 활용
    @Override
    public int compareTo(BoardImage other) {
        return this.ord - other.ord;
    }

    public void changeBoard(Board board) {
        this.board = board;
    }

}
