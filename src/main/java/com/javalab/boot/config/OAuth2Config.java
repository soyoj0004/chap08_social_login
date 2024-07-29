package com.javalab.boot.config;

import com.javalab.boot.repository.MemberRepository;
import com.javalab.boot.security.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * [OAuth2Config 클래스]
 * - OAuth2UserService 빈 등록
 * - OAuth2UserService 인터페이스를 구현한 CustomOAuth2UserService 빈 등록
 */
@Configuration
public class OAuth2Config {

    /**
     * [customOAuth2UserService 메소드]
     * - OAuth2UserService 빈 등록
     * - OAuth2UserService 인터페이스를 구현한 CustomOAuth2UserService 빈 등록
     * @param memberRepository : MemberRepository 객체
     * @param passwordEncoder : PasswordEncoder 객체
     * @return OAuth2UserService<OAuth2UserRequest, OAuth2User> : OAuth2UserService 객체
     */
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        return new CustomOAuth2UserService(memberRepository, passwordEncoder);
    }
}
