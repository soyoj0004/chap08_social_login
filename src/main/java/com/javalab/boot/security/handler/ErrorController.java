package com.javalab.boot.security.handler;

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
    public String accessDenied(Model model) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        model.addAttribute("username", username);
        model.addAttribute("error", "해당 페이지에 접근 권한이 없습니다.");
      
        log.info("accessDenied username : " + username);
        
        return "error/access-denied"; 
    }
}
