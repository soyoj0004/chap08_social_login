package com.javalab.boot.entity;

import com.javalab.boot.constant.Role;
import com.javalab.boot.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;


@Entity
@Table(name="member")
@Getter @Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // unique 옵션을 주면 테이블 생성시 유니크한 컬럼으로 만들어짐.
    @Column(unique = true)
    private String email;

    private String password;

    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    // two attributes are added for social login
    private boolean del;
    private boolean social;

    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder){
        // 비밀번호 암호화
        String password = passwordEncoder.encode(memberFormDto.getPassword());

        Member member = Member.builder()
                .name(memberFormDto.getName())
                .email(memberFormDto.getEmail())
                .address(memberFormDto.getAddress())
                .password(password) // 암호화된 비밀번호 세팅
                .role(Role.ADMIN)
                .del(false)
                .social(false)
                .build();
        return member;
    }

}
