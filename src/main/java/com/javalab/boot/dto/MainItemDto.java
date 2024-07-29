package com.javalab.boot.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MainItemDto {

    private Long id;

    private String itemNm;

    private String itemDetail;

    private Integer price;

    //private String imgUrl;

    @QueryProjection
    public MainItemDto(Long id, String itemNm, String itemDetail,
                       //String imgUrl,
                       Integer price){
        this.id = id;
        this.itemNm = itemNm;
        this.itemDetail = itemDetail;
        //this.imgUrl = imgUrl;
        this.price = price;
    }

}