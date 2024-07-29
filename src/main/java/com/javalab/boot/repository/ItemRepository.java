package com.javalab.boot.repository;

import com.javalab.boot.entity.Board;
import com.javalab.boot.entity.Item;
import com.javalab.boot.repository.search.ItemSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemSearch {

    // 상품명으로 조회 메소드
    List<Item> findByItemNm(String itemNm);

    // 상품명으로 조회 메소드 (like 검색)
    List<Item> findByItemNmContaining(String itemNm);

    // 상품명 or 상품상세설명 컬럼으로 검색
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail);

    // 가격이 1000보다 크고 5000보다 작은 상품 조회 메소드
    List<Item> findByPriceGreaterThanAndPriceLessThan(Integer minPrice, Integer maxPrice);

    // [JPQL] 상품 상세 설명으로 Like 검색
    @Query("select i from Item i where i.itemDetail like " +
            "%:itemDetail% order by i.price desc")
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);

    // [JPQL] 상품 상세 설명 + 상품가격 Between + 정렬조건으로 검색
    @Query("select i from Item i where i.itemDetail like %:itemDetail% " +
            " and i.price > :price1 and i.price < :price2 order by i.price desc")
    List<Item> findByItemDetailAndPriceRange(@Param("itemDetail") String itemDetail,
                                             @Param("price1") Integer price1,
                                             @Param("price2") Integer price2);
    // [Native SQL] nativeQuery = true
    @Query(value="select i.* from item i where i.item_detail like " +
            "%:itemDetail% order by i.price desc", nativeQuery = true)
    List<Item> findByItemDetailByNative(@Param("itemDetail") String itemDetail);

    // 추가 10.27

    /**
     * @EntityGraph :
     *  - Item 과 ItemImg가 지연로딩 관계이지만
     *  필요에 따라서 즉시 로딩되도록 해줌.
     * attributePaths :
     *  - Item 조회시 즉시 조회할 엔티티(속성)를 명시.
     *  - Item 조회시 자신의 imageSet 속성을 EAGER 로딩 하겠다.
     */
    @EntityGraph(attributePaths = {"imageSet"})
    @Query("select i from Item i where i.id =:id")
    Optional<Item> findByIdWithImages(@Param("id") Long id);

    // 미사용
    @EntityGraph(attributePaths = {"imageSet"})
    @Query("select i from Item i where (:types IS NULL OR i.itemSellStatus IN :types) " +
            "and (i.itemNm like %:keyword% or i.itemDetail like %:keyword%)")
    Page<Item> findByIdWithImages2(
            @Param("types") String[] types,
            @Param("keyword") String keyword,
            Pageable pageable
    );


}