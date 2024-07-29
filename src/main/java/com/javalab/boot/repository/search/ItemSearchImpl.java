package com.javalab.boot.repository.search;

import com.javalab.boot.constant.ItemSellStatus;
import com.javalab.boot.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

/**
 * 1. 이 클래스(ItemSearchImpl)는 Spring Data JPA와 Querydsl을
 *   함께 사용하여 Item 엔티티에 대한 사용자 정의 쿼리 메서드를 제공하는 역할
 * - QuerydslRepositorySupport는 JPQL 쿼리를 쉽게 작성하고
 *  실행할 수 있는 여러 도움 메서드를 제공하는 클래스.
 *  이를 통해 타입 안전한 쿼리를 작성 가능.
 */
@Log4j2
public class ItemSearchImpl extends QuerydslRepositorySupport implements ItemSearch {

    /**
     * QuerydslRepositorySupport는 엔티티 타입에 따른 Querydsl
     * 쿼리를 작성하게 도와주는데, 생성자에서 사용할 엔티티의
     * 타입(Item.class)을 지정합니다. 이로써 Item 엔티티에
     * 대한 Querydsl 쿼리 작성이 가능해집니다.
     */
    public ItemSearchImpl() {
        super(Item.class);
    }

    @Override
    public Page<Item> search(Pageable pageable) {

        QItem qItem = QItem.item;

        // from 메서드를 사용하여 Item 엔티티에 대한 JPQL 쿼리를 초기화
        JPQLQuery<Item> query = from(qItem);

        // Querydsl에 제공될 조건문을 동적으로 생성할 수 있는 빌더 클래스
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // or 상품명 or 상품상세설명 필드 Like 검색 조건 생성
        booleanBuilder.or(qItem.itemNm.contains("상품")); // 상품명 like ...
        booleanBuilder.or(qItem.itemDetail.contains("상품")); // 상품상세설명 like ....

        // where or 조건
        query.where(booleanBuilder);
        // where and 조건
        query.where(qItem.id.gt(0L));

        /*
         * 페이징
         * query : JPQL 쿼리객체
         * this.getQuerydsl() : 반환 타입이 Querydsl
         * applyPagination() : Querydsl에 Pageable 적용하여
         *  페이징 처리하는 역할.
         */
        this.getQuerydsl().applyPagination(pageable, query);

        // JPQLQuery 쿼리 실행
        List<Item> list = query.fetch();

        long count = query.fetchCount();

        return new PageImpl<>(list, pageable, count);

    } // end searchOnlyPaging

    @Override
    public Page<Item> searchCondition(String[] types, String keyword, Pageable pageable) {

        QItem qItem = QItem.item;
        JPQLQuery<Item> query = from(qItem);

        if( (types != null && types.length > 0) && keyword != null ){ //검색 조건과 키워드가 있다면

            BooleanBuilder booleanBuilder = new BooleanBuilder(); //

            for(String type: types){

                switch (type){
                    case "n":   // 상품명
                        booleanBuilder.or(qItem.itemNm.contains(keyword));
                        break;
                    case "d":   // 상품상세설명
                        booleanBuilder.or(qItem.itemDetail.contains(keyword));
                        break;
                    case "s":   // 판매상태
                        // ItemSellStatus 열거형(Enum)을 사용하여 검색 조건
                        if ("SELL".equals(keyword) || "SOLD_OUT".equals(keyword)) {
                            booleanBuilder.or(qItem.itemSellStatus.eq(ItemSellStatus.valueOf(keyword)));
                        }
                }
            }//end for
            query.where(booleanBuilder);
        }//end if

        query.where(qItem.id.gt(0L));

        this.getQuerydsl().applyPagination(pageable, query);

        // fetch 결과 List<Item>
        List<Item> list = query.fetch();
        // fetchCount 전체 레코드 갯수 가져옴.
        long count = query.fetchCount();
        /*
          Spring Data의 Page 인터페이스의 구현체
          Page 인터페이스는 페이징 처리와 관련된 여러 메서드와
          정보를 제공. PageImpl<>을 사용 페이징된 데이터
          관련된 정보를 캡슐화 함.
         */
        return new PageImpl<>(list, pageable, count);

    }
}
