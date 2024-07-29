package com.javalab.boot.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Log4j2
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.info("--------ACCESS DENIED--------------");

        // 접근 거부 처리 로직을 여기에 구현합니다.
        // 예를 들어, 사용자에게 접근이 거부되었음을 알리는 메시지를 보여줄 수 있습니다.
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("error", "이 페이지에 접근 권한이 없습니다.");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        modelAndView.setViewName("redirect:/access-denied");
        response.sendRedirect("/access-denied"); // ErrorController 컨트롤러 요청

    }

}
