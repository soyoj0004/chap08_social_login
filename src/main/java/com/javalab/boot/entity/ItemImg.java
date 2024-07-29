package com.javalab.boot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemImg implements Comparable<ItemImg>{

    @Id
    private String uuid;
    private String fileName; // 파일명
    private String repimgYn; // 대표 이미지 여부
    private int ord; // 순서
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;
    @Override
    public int compareTo(ItemImg other) {
        return this.ord - other.ord;
    }
    public void changeBoard(Item item) {
        this.item = item;
    }

}