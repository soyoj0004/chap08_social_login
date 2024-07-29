package com.javalab.boot.controller;

import com.javalab.boot.dto.*;
import com.javalab.boot.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping("/")
    public String list(@ModelAttribute("pageRequestDTO") PageRequestDTO pageRequestDTO,
                       Model model) {
        PageResponseDTO<ItemFormDTO> responseDTO = itemService.list(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        return "main";
    }

}