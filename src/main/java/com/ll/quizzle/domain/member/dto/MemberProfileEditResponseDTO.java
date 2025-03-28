package com.ll.quizzle.domain.member.dto;

import com.ll.quizzle.domain.member.entity.Member;

public record MemberProfileEditResponseDTO(
	Long id,
	String profile
) {
	public static MemberProfileEditResponseDTO from(Member member) {
		return new MemberProfileEditResponseDTO(member.getId(), member.getNickname());
	}
}

