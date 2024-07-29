package com.javalab.boot.repository;


import com.javalab.boot.constant.Role;
import com.javalab.boot.entity.Member;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
@Transactional
@Rollback(value = false)
public class MemberTests {

    @Autowired
    private MemberRepository repository;

    // 스프링 시큐리티가 제공하는 암호화 클래스
    @Autowired
    private PasswordEncoder passwordEncoder;

    //@Disabled
    @Test
    public void testCreateMember() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 사용자 생성
//        Member member = Member.builder()
//                        .email("user1@zerock.org")
//                        .address("서울시")
//                        .name("홍길동")
//                        .password(passwordEncoder.encode("1111"))
//                        .role(Role.USER)
//                        .build();
//        repository.save(member);

//        Member member = Member.builder()
//                        .email("user2@zerock.org")
//                        .address("경기도")
//                        .name("김길동")
//                        .password(passwordEncoder.encode("1111"))
//                        .role(Role.ADMIN)
//                        .build();
//        repository.save(member);
        Member member = Member.builder()
                        .email("user3@zerock.org")
                        .address("경기도")
                        .name("박길동")
                        .password(passwordEncoder.encode("1111"))
                        .role(Role.ADMIN)
                        .build();
        repository.save(member);

        // validation
        assertEquals("user3@zerock.org", member.getEmail());
    }


    @Test
    public void testEncode() {

        String password = "1111";

        // 암호화된 비밀번호
        String enPw = passwordEncoder.encode(password);

        System.out.println("암호화된 비밀번호 : " + enPw);

        boolean matchResult = passwordEncoder.matches(password, enPw);

        System.out.println("matchResult: " + matchResult);

    }


//    @Test
//    public void insertDummies() {
//
//        //1 - 80까지는 USER만 지정
//        //81- 90까지는 USER,MANAGER
//        //91- 100까지는 USER,MANAGER,ADMIN
//
//        IntStream.rangeClosed(1,3).forEach(i -> {
//            Member member = Member.builder()
//                    .email("user"+i+"@zerock.org")
//                    .name("사용자"+i)
//                    .roleSet(new HashSet<MemberRole>())
//                    .password(  passwordEncoder.encode("1111") )
//                    .build();
//
//            //default role
//            Member.addMemberRole(MemberRole.USER);
//
//            if(i > 80){
//                Member.addMemberRole(MemberRole.MANAGER);
//            }
//
//            if(i > 90){
//                Member.addMemberRole(MemberRole.ADMIN);
//            }
//
//            repository.save(Member);
//
//        });
//
//    }

//    @Test
//    public void testRead() {
//
//        Optional<Member> result = repository.findByEmail("user95@zerock.org", false);
//
//        Member Member = result.get();
//
//        System.out.println(Member);
//
//    }


}
