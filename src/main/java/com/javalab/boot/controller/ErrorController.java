package com.javalab.boot.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 접근 권한이 없을 경우 처리 컨트롤러
 *  - 시큐리티 환경설정 파일에 설정됨.
 *    http.exceptionHandling().accessDeniedPage("/access-denied");
 */
@Controller
@Log4j2
public class ErrorController {

    @GetMapping("/access-denied")
    public String accessDenied(Model model, HttpServletResponse response) {

        model.addAttribute("error", "접근 권한이 없습니다.");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 상태 코드(302) 설정

        return "error/access-denied"; 
    }
}
