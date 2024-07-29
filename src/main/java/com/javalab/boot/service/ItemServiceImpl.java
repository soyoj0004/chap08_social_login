package com.javalab.boot.service;

import com.javalab.boot.dto.*;
import com.javalab.boot.entity.Board;
import com.javalab.boot.entity.BoardImage;
import com.javalab.boot.entity.Item;
import com.javalab.boot.entity.ItemImg;
import com.javalab.boot.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional // 데이터베이스 트랜잭션 처리용
public class ItemServiceImpl implements ItemService{
    // 생성자 의존성 주입됨.
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;

    /**
     * Item 등록
     */
    @Override
    public Long register(ItemFormDTO itemFormDTO) {
        Item item = itemFormDTO.createItem();
        log.info("Service register 변환된 Item : " + item.getImageSet());
        Long itemId = itemRepository.save(item).getId();
        return itemId;
    }

    /**
     * Item 상세보기
     * @param itemId
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public ItemFormDTO readOne(Long itemId) {
        /*
          [Item 엔티티 객체 얻기]
          findByIdWithImages() 메소드에는 @EntityGraph가 있어서
          한번 쿼리할 때 BoardImage들도 동시에 Left Join 해서 갖고옴.
        */
        Optional<Item> result = itemRepository.findByIdWithImages(itemId);
        Item item = result.orElseThrow();

        // 엔티티 -> Dto 로 변환
        ItemFormDTO itemFormDTO = item.entityToDto();
        log.info("Service readOne 메소드 첨부 이미지 갯수 : " + itemFormDTO.getFileNames().size());

        return itemFormDTO;
    }

    /**
     * Item 수정
     * @param itemFormDTO
     */
    @Override
    public void modify(ItemFormDTO itemFormDTO) {
        // 수정할 게시물 조회해서 영속 영역에 보관
        Optional<Item> result = itemRepository.findById(itemFormDTO.getId());

        // 영속 영역의 엔티티 참조 얻기
        Item item = result.orElseThrow();

        /*
          영속 영역에 있는 엔티티 객체의 값 변경하면 디티체킹 대상이 됨.
          더티 체킹은 최초의 상태와 지금의 상태 비교해서 서로 달라진 경우.
          JPA는 트랜잭션의 커밋 시점에 이 더티체킹을 수행하여, 변경된
          엔티티에 대한 UPDATE SQL을 데이터베이스에 실행.
        */
        item.updateItem(itemFormDTO);

        // 기존 첨부 파일들은 정리 즉, 참조하고 있는 Item Image 객체들의
        // item 속성을 null로 처리, 상위 객체의 참조 제거됨. 고아객체됨.
        // orphanRemoval = true로 고아 객체 자동 삭제 대상
        item.clearImages();

        // 저장할 파일들이 전달되어 온 경우
        if(itemFormDTO.getFileNames() != null){
            log.info("첨부 이미지 존재");
            List<String> fileNames = itemFormDTO.getFileNames();
            for (int i = 0; i < fileNames.size(); i++) {
                String fileName = fileNames.get(i);
                log.info("fileName : " + fileName);
                String[] arr = fileName.split("_");
                String repimgYn = (i == 0) ? "Y" : "N";
                item.addImage(arr[0], arr[1], repimgYn);
            }
        }

        log.info("board imageSet size() : " + item.getImageSet().size());
        itemRepository.save(item);
    }

    /**
     * 삭제
     * @param itemId
     */
    @Override
    public void remove(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    /**
     * 목록 조회 (No Searching)
     */
//    @Override
//    public PageResponseDTO<ItemFormDTO> list(PageRequestDTO pageRequestDTO) {
//
//        String[] types = pageRequestDTO.getTypes();
//        String keyword = pageRequestDTO.getKeyword();
//        Pageable pageable = pageRequestDTO.getPageable("id");
//
//        // result : 1.엔티티 목록, 2.페이징 관련 정보
//        Page<Item> result = itemRepository.search(pageable);
//
//        List<ItemFormDTO> dtoList = result.getContent().stream()
//                .map(item -> modelMapper.map(item, ItemFormDTO.class))
//                .collect(Collectors.toList());
//
//        PageResponseDTO<ItemFormDTO> pageResponseDTO =
//                PageResponseDTO.<ItemFormDTO>builder()
//                        .pageRequestDTO(pageRequestDTO) /* 페이지 요청정보 */
//                        .dtoList(dtoList) /* 목록 데이터 */
//                        .total((int)result.getTotalElements())
//                        .build();
//
//        return pageResponseDTO;
//    }

    /**
     * 목록 조회 (Searching)
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ItemFormDTO> list(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<Item> result = itemRepository.searchCondition(types, keyword, pageable);

        // modelMapper를 통해서 Entity -> Dto 변환
        List<ItemFormDTO> dtoList = result.getContent().stream()
                .map(item -> modelMapper.map(item, ItemFormDTO.class))
                .collect(Collectors.toList());

        // 변환시킨 Dto, PageRequest 등을 PageResponseDTO 저장
        PageResponseDTO<ItemFormDTO> pageResponseDTO = PageResponseDTO.<ItemFormDTO>builder()
                        .pageRequestDTO(pageRequestDTO)
                        .dtoList(dtoList)
                        .total((int)result.getTotalElements())
                        .build();

        return pageResponseDTO;
    }

}
