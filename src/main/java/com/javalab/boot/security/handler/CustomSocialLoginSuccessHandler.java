package com.javalab.boot.security.handler;


import com.javalab.boot.security.dto.MemberSecurityDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;


@Log4j2
@RequiredArgsConstructor
public class CustomSocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final PasswordEncoder passwordEncoder;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("----------------------------------------------------------");
        log.info("여기는 CustomLoginSuccessHandler onAuthenticationSuccess ..........");
        log.info(authentication.getPrincipal());

        // 시큐리티에 보관한 정보 추출
        MemberSecurityDTO memberSecurityDTO = (MemberSecurityDTO) authentication.getPrincipal();

        //String encodedPw = memberSecurityDTO.getPassword();

        // 소셜 로그인이고 회원의 패스워드가 1111이라면
        // 처음에 비밀번호를 1111로 강제로 넣어두었음
        log.info("memberSecurityDTO.isSocial() : " + memberSecurityDTO.isSocial());
        boolean boolPass = passwordEncoder.matches("1111", memberSecurityDTO.getPassword());
        log.info("passwordEncoder.matches(1111) 맞나? : " + boolPass);


        // 비밀번호가 1111이라는 얘기는 아직 안바꿨다는 의미
        if (memberSecurityDTO.isSocial() && (memberSecurityDTO.getPassword().equals("1111")
                ||  passwordEncoder.matches("1111", memberSecurityDTO.getPassword()))) {
            log.info("소셜 로그인으로 수정화면으로 이동합니다.");
            log.info("회원정보 수정화면으로 이동합니다. ");
            response.sendRedirect("/member/modify");
            return;
        } else {    // 비밀번호가 1111이 아니라는 얘기는 바꿨다는 의미
            log.info("비밀번호가 1111이 아니므로 게시판 리스트로 이동합니다.");
            response.sendRedirect("/board/list");
        }
    }
}
