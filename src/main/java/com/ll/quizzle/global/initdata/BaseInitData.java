package com.ll.quizzle.global.initdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;

import lombok.RequiredArgsConstructor;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class BaseInitData {
	private final MemberRepository memberRepository;
	private final OAuthRepository oAuthRepository;

	@Autowired
	@Lazy
	private BaseInitData self;

	@Bean
	public ApplicationRunner baseInitDataRunner() {
		return args -> {
			self.init();
		};
	}

	@Transactional
	public void init() {
		if (memberRepository.count() > 0) {
			return;
		}

		for (int i = 1; i <= 50; i++) {
			String nickname = String.format("%s%d", "test", i);
			String email = String.format("%s%d@email.com", "test", i);
			String provider = (i % 2 == 0) ? "google" : "kakao";

			Member member = Member.create(nickname, email);

			OAuth oauth = OAuth.create(member, provider, String.valueOf(i));

			memberRepository.save(member);
			oAuthRepository.save(oauth);
		}
	}
}
