package com.ll.quizzle.factory;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;

public class TestMemberFactory {

	public static Member createOAuthMember(String nickname, String email, String provider, String oauthId,
		MemberRepository memberRepo, OAuthRepository oauthRepo) {
		Member member = Member.create(nickname, email);
		memberRepo.save(member);

		OAuth oauth = OAuth.create(member, provider, oauthId);
		oauthRepo.save(oauth);

		return member;
	}
}