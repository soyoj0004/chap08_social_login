package com.javalab.boot.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * 접근 거부 핸들러 클래스
 * - 사용자가 어떤 URL을 요청했을 그 URL에 대한 권한이 없을 때 작동하는 객체
 * - 시큐리티 환경 설정 파일에 설정해놓음
 */
@Log4j2
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.info("--------ACCESS DENIED--------------");
        log.info("Request URI: " + request.getRequestURI());
        log.info("Exception: " + accessDeniedException.getMessage());

        response.sendRedirect("/access-denied"); // ErrorController 컨트롤러 요청
    }

}
