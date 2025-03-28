package com.ll.quizzle.domain.member.dto;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.type.Role;

// 테스트를 위한 임시 Record 코드
public record MemberDto(
	String nickName,
	String email,
	int level,
	Role role
) {
	// Member 를 MemberDTO 로 전환
	public static MemberDto from(Member member) {
		return new MemberDto(
			member.getNickname(),
			member.getEmail(),
			member.getLevel(),
			Role.MEMBER
		);
	}
}
