package com.ll.quizzle.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.quizzle.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String memberEmail);

    Optional<Member> findByNickname(String nickname);

    boolean existsByNickname(String nickname);
}
