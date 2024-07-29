package com.javalab.boot.handler;

import com.javalab.boot.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 로그인 성공시 다음 처리를 담당하는 클래스
 *  - 예를 들면 메인 페이지로 이동할 수도 있다.
 */
@RequiredArgsConstructor
@Component
public class AuthSucessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	private final MemberRepository memberRepository;
	
	@Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        //memberRepository.updateMemberLastLogin(authentication.getName(), LocalDateTime.now());
        setDefaultTargetUrl("/"); // 컨트롤러에 "/" 요청
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
