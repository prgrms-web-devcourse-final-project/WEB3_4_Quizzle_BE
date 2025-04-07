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
import com.ll.quizzle.domain.member.type.Role;
import com.ll.quizzle.domain.system.service.SystemService;
import com.ll.quizzle.global.security.oauth2.entity.OAuth;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;

import lombok.RequiredArgsConstructor;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class BaseInitData {
    private final MemberRepository memberRepository;
    private final OAuthRepository oAuthRepository;
    private final SystemService systemService;

    @Autowired
    @Lazy
    private BaseInitData self;

    @Bean
    public ApplicationRunner baseInitDataRunner() {
        return args -> {
            self.init();
            self.adminInit();
        };
    }

    @Transactional
    public void init() {
        if (memberRepository.count() > 0) {
            return;
        }

        for (int i = 1; i <= 10; i++) {
            String nickname = String.format("%s%d", "test", i);
            String email = String.format("%s%d@email.com", "test", i);
            String provider = (i % 2 == 0) ? "google" : "kakao";

            Member member = Member.create(nickname, email);

            OAuth oauth = OAuth.create(member, provider, String.valueOf(i));

            memberRepository.save(member);
            oAuthRepository.save(oauth);
        }
    }

    @Transactional
    public void adminInit() {
        if (memberRepository.findByEmail("admin@quizzle.com").isPresent()) {
            return;
        }

        if (memberRepository.findByEmail("member@quizzle.com").isPresent()) {
            return;
        }

        Member testAdmin = Member.create("admin", "admin@quizzle.com");
        testAdmin.changeRole(Role.ADMIN);

        Member testMember = Member.create("member", "member@quizzle.com");

        OAuth testAdminOauth = OAuth.create(testAdmin, "kakao", "51");
        OAuth testMemberOauth2 = OAuth.create(testMember, "google", "52");

        memberRepository.save(testAdmin);
        memberRepository.save(testMember);
        oAuthRepository.save(testAdminOauth);
        oAuthRepository.save(testMemberOauth2);
    }
}
