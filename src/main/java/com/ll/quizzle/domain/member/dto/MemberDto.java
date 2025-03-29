package com.ll.quizzle.domain.member.dto;

import com.ll.quizzle.domain.member.entity.Member;

public record MemberDto(
	Long id,
	String nickname,
	String email,
	int level,
	String profilePath,
	boolean online
) {
	public static MemberDto from(Member member, boolean online) {
		return new MemberDto(
			member.getId(),
			member.getNickname(),
			member.getEmail(),
			member.getLevel(),
			member.getProfilePath(),
			online
		);
	}
}
