package com.ll.quizzle.domain.avatar.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ll.quizzle.domain.avatar.entity.Avatar;
import com.ll.quizzle.domain.avatar.type.AvatarStatus;
import com.ll.quizzle.domain.member.entity.Member;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    List<Avatar> findByStatus(AvatarStatus status);
    List<Avatar> findByMemberAndStatus(Member member, AvatarStatus status);
    Optional<Avatar> findByFileName(String fileName);

}
