package com.javalab.boot.service;

import com.javalab.boot.dto.ItemFormDTO;
import com.javalab.boot.dto.PageRequestDTO;
import com.javalab.boot.dto.PageResponseDTO;

public interface ItemService {
    Long register(ItemFormDTO itemFormDTO);
    ItemFormDTO readOne(Long itemId);
    void modify(ItemFormDTO itemFormDTO);
    void remove(Long itemId);
//    PageResponseDTO<ItemFormDTO> list(PageRequestDTO pageRequestDTO);

    PageResponseDTO<ItemFormDTO> list(PageRequestDTO pageRequestDTO);

}
