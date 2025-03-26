package com.ll.quizzle.domain.member.dto;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.type.Role;

public record MemberDto(
	Long id,
	String nickName,
	Role role
) {
	// Member 를 MemberDTO 로 전환
	public static MemberDto from(Member member) {
		return new MemberDto(
			member.getId(),
			member.getNickName(),
			member.getRole()
		);
	}
}
