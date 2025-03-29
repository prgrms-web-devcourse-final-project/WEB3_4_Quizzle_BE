package com.ll.quizzle.domain.member.repository;

import com.ll.quizzle.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String memberEmail);

    // OAuth 정보로 회원을 조회하는 메서드
    @Query("SELECT m FROM Member m WHERE m.id IN " +
            "(SELECT o.member.id FROM OAuth o WHERE o.provider = :provider AND o.oauthId = :oauthId)")
    Optional<Member> findByProviderAndOauthId(
            @Param("provider") String provider,
            @Param("oauthId") String oauthId
    );
}
