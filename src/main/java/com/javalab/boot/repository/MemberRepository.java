package com.javalab.boot.repository;

import com.javalab.boot.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);

//    필요시 최종 로그인 시간 업데이트
//    static final String UPDATE_MEMBER_LAST_LOGIN = "UPDATE Member "
//            + "SET LAST_LOGIN_TIME = :lastLoginTime "
//            + "WHERE EMAIL = :email";
//    @Transactional
//    @Modifying
//    @Query(value=UPDATE_MEMBER_LAST_LOGIN, nativeQuery = true)
//    public int updateMemberLastLogin(@Param("email") String email, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * [소셜 로그인 사용자가 일반사용자로 전환]
     *  - 소셜 로그인으로 만들어진 사용자의 비밀번호 변경
     *  - 해당 사용자의 소설로그인 정보(social) 컬럼을 '0'(false)로 변경
     *
     * @Modifying : C/U/D처리 가능
     */
    @Modifying
    @Transactional
    @Query("update Member m set m.password =:password, m.social = false  where m.email = :email ")
    void updatePasswordAndSocial(@Param("password") String password, @Param("email") String email);
}