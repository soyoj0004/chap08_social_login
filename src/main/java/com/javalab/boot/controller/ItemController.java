package com.javalab.boot.controller;

import com.javalab.boot.dto.ItemFormDTO;
import com.javalab.boot.dto.PageRequestDTO;
import com.javalab.boot.dto.PageResponseDTO;
import com.javalab.boot.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/*********************************************************
 * ItemController
 ********************************************************/
@Controller
@RequestMapping("/item")
@RequiredArgsConstructor
@Log4j2
public class ItemController {

    private final ItemService itemService;

    /**
     * 공통메소드
     *  - 이 컨트롤러의 모든 메소드가 호출되면 그 전에 이 메소드가 호출됨.
     *  - 이 메소드의 반환값은 자동으로 model에 저장됨.
     *  - model에 저장될 때 "roles"라는 이름으로 저장됨.
     */
    @ModelAttribute("roles")
    public Map<String, String> getRoles() {
        Map<String, String> roles = new HashMap<>();
        roles.put("manager", "Manager");
        roles.put("user", "User");
        return roles;
    }

    //@PreAuthorize("hasRole('ADMIN')") // 등록은 관리자만 가능하도록 설정
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("itemFormDTO", new ItemFormDTO());
        return "item/register";
    }

    @PostMapping("/register")
    public String register(@Valid ItemFormDTO itemFormDTO,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        log.info("itemFormDTO : " + itemFormDTO);
        if(bindingResult.hasErrors()) {
            log.info("등록 화면 오류 있음");
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "item/register";
        }
        Long itemId = itemService.register(itemFormDTO);
        return "redirect:/item/list";
    }

    @GetMapping("/list")
    public String list(@ModelAttribute("pageRequestDTO") PageRequestDTO pageRequestDTO,
                       Model model) {
        PageResponseDTO<ItemFormDTO> responseDTO = itemService.list(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        return "item/list";
    }

    @GetMapping({"/read"})
    public String read(@RequestParam("id") Long id,
                       @ModelAttribute("pageRequestDTO") PageRequestDTO pageRequestDTO,
                       Model model) {
        ItemFormDTO itemFormDTO = itemService.readOne(id);
        model.addAttribute("item", itemFormDTO);
        return "item/read";
    }

    //@PreAuthorize("hasAuthority('ADMIN')") // 상품 수정도 관리자만 가능하도록 설정
    @GetMapping({"/modify"})
    public String modifyGet(@RequestParam("id") Long id, @ModelAttribute("pageRequestDTO") PageRequestDTO pageRequestDTO, Model model) {
        ItemFormDTO itemFormDTO = itemService.readOne(id);
        model.addAttribute("item", itemFormDTO);
        return "item/modify";
    }

    @PostMapping("/modify")
    public String modify(PageRequestDTO pageRequestDTO,
                         @Valid @ModelAttribute("item") ItemFormDTO itemFormDTO,
                         BindingResult bindingResult,
                         Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "item/modify"; // 수정폼으로 다시 이동
        }
        itemService.modify(itemFormDTO);
        return "redirect:/item/read?id=" + itemFormDTO.getId() + "&" + pageRequestDTO.getLink();
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("id") Long id,
                         @ModelAttribute("pageRequestDTO") PageRequestDTO pageRequestDTO) {
        itemService.remove(id);
        return "redirect:/item/list?" + pageRequestDTO.getLink();
    }
}
