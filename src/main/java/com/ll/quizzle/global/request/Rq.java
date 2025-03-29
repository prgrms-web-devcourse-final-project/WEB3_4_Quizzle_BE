package com.ll.quizzle.global.request;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.security.oauth2.dto.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

@Component
@RequestScope
@RequiredArgsConstructor
@Slf4j
public class Rq {
    private final MemberRepository memberRepository;
    private Member actor;

    public Member getActor() {
        if (actor == null) {
            actor = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                    .map(Authentication::getPrincipal)
                    .filter(principal -> principal instanceof SecurityUser)
                    .map(principal -> (SecurityUser) principal)
                    .flatMap(securityUser -> memberRepository.findByProviderAndOauthId(
                            securityUser.getProvider(),
                            securityUser.getOauthId()
                    ))
                    .orElseThrow(ErrorCode.UNAUTHORIZED::throwServiceException);
        }

        return actor;
    }
}