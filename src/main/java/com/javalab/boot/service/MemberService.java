package com.javalab.boot.service;

import com.javalab.boot.entity.Member;
import com.javalab.boot.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    //private final PasswordEncoder passwordEncoder;
    public Member saveMember(Member member){
        validateDuplicateMember(member);

        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member){
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    // 실제 인증 진행(DB에 이메일로 사용자 정보 조회해옴)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }

        // 반환타입이 UserDetails 이므로 그 자손인 User객체로 변환(Member -> User변환)
        return User.builder()
                .username(member.getEmail()) // 이메일
                .password(member.getPassword()) // 비밀번호
                .roles(member.getRole().toString()) // 권한
                .build();
    }

    /**
     * 카카오 소셜 로그인 사용자 비밀번호 변경.
     * 카카오 소셜 로그인 사용자의 social 컬럼값을 0(false)로 변경
     * 즉, 일반사용자로 전환되서 아이디/비밀번호로 로그인 가능.
     */
    @Transactional
    public void modifyPasswordAndSocialStatus(String email, String encodedPassword) {

        // 비밀번호 변경
        memberRepository.updatePasswordAndSocial(encodedPassword, email);
    }

}