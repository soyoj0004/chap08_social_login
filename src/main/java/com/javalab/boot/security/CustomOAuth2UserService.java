package com.javalab.boot.security;

import com.javalab.boot.constant.Role;
import com.javalab.boot.entity.Member;
import com.javalab.boot.repository.MemberRepository;
import com.javalab.boot.security.dto.MemberSecurityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * [CustomOAuth2UserService 클래스]
 * - DefaultOAuth2UserService 클래스를 상속받아 구현한 클래스
 * - OAuth2UserService 인터페이스를 구현한 CustomOAuth2UserService 빈 등록
 */
@Log4j2
//@Service  // OAuth2Config에서 빈으로 등록되므로 주석처리함.
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * [loadUser 메소드]
     * - OAuth2UserRequest 객체를 전달받아 OAuth2User 객체를 반환하는 메소드
     * - OAuth2UserRequest 객체는 사용자의 OAuth2 인증 요청에 대한 정보(AccessToken)를 포함하고 있지만,
     *   사용자의 이메일, 이름 등의 상세한 정보는 포함하고 있지 않다.
     * - loadUser 메서드에서는 이 OAuth2UserRequest를 사용하여 카카오 인증 서버로 사용자 정보를 요청하고,
     *   그 결과를 바탕으로 로컬 데이터베이스에서 사용자를 인증하고 사용자의 세부 정보를 로드하는 작업을 수행하여
     *   시큐리티 인증을 위한 객체로 사용함.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("userRequest....{}", userRequest);

        ClientRegistration clientRegistration = userRequest.getClientRegistration(); // 클라이언트 정보를 가져옴, 예를들면 카카오, 구글 등
        String clientName = clientRegistration.getClientName(); // 클라이언트 이름을 가져옴, 예를들면 카카오

        log.info("clientName {} ",  clientName); // 소셜 회사

        // OAuth2User 가 포함하는 정보는 소셜 로그인회사에서 받아온 정보들이다.
        OAuth2User oAuth2User = super.loadUser(userRequest);    // 부모의 loadUser(소셜로그인회사, 액세스토큰) OAuth2User 객체를 가져옴

        Map<String, Object> paramMap = oAuth2User.getAttributes(); // 소셜 로그인회사에서 받아온 정보들을 paramMap에 저장

        String email = null;

        switch (clientName) {
            case "kakao":
                email = getKakaoEmail(paramMap);
                break;
        }

        log.info("===============================");
        log.info("카카오에서 받아온 이메일 : " + email);
        log.info("===============================");

        // PasswordEncoder를 파라미터로 전달받아 generateDTO 호출
        return generateDTO(email, paramMap);
    }

    private MemberSecurityDTO generateDTO(String email, Map<String, Object> params) {
        // 소셜 로그인 회사에서 받은 이메일로 우리 데이터베이스에서 회원 정보를 조회
        Member result = memberRepository.findByEmail(email);
        // 회원 정보가 없다면 회원 정보를 생성하여 저장, 즉 최초 소셜 로그인 진행자
        if (result == null) {
            // 최초 소셜 로그인한 사용자를 DB에 저장
            Member member = Member.builder()
                    .password(passwordEncoder.encode("1111")) // 소셜로그인 사용자는 비밀번호를 1111로 세팅
                    .email(email)
                    .social(true)       // 소셜 로그인 플래그 true
                    .role(Role.USER)    // 소셜 로그인 사용자는 USER 권한을 가짐
                    .del(false)
                    .build();
            memberRepository.save(member); // 영속화
            // Authentication 객체로 만들 DTO를 생성하여 반환
            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    email,
                    "1111",
                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            memberSecurityDTO.setProps(params); // 소셜 로그인 정보를 저장
            memberSecurityDTO.setSocial(true);  // 소셜 로그인 플래그 true

            return memberSecurityDTO;
        } else {    // 이전에 소셜 로그인해서 데이터베이스 정보가 있는 경우
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

            authorities.add(new SimpleGrantedAuthority(result.getRole().name()));
            // 데이터베이스에서 읽어온 정보로 인증객체 생성
            MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                    result.getEmail(),
                    result.getPassword(),
                    authorities
            );
            memberSecurityDTO.setSocial(true);
            return memberSecurityDTO;
        }
    }

    private String getKakaoEmail(Map<String, Object> paramMap) {
        log.info("KAKAO-----------------------------------------");
        Object value = paramMap.get("kakao_account"); // 카카오에서 받아온 정보 중 kakao_account 정보를 가져옴
        log.info(value);
        LinkedHashMap accountMap = (LinkedHashMap) value; // kakao_account 정보를 LinkedHashMap으로 변환
        String email = (String) accountMap.get("email"); // LinkedHashMap에서 email 정보를 가져옴
        log.info("email..." + email);
        return email;
    }
}