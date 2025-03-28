package com.ll.quizzle.global.security.oauth2;


import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.service.AuthTokenService;
import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.global.security.oauth2.dto.SecurityUser;
import com.ll.quizzle.standard.util.Ut;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthTokenService authTokenService;
    private final MemberService memberService;

    @Value("${app.oauth2.authorizedRedirectUris}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        log.debug("OAuth2 login success - provider: {}, oauthId: {}, email: {}",
                securityUser.getProvider(), securityUser.getOauthId(), securityUser.getEmail());

        if (securityUser.isNewUser()) {
            String redirectUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                    .queryParam("provider", URLEncoder.encode(securityUser.getProvider(), StandardCharsets.UTF_8))
                    .queryParam("oauthId", URLEncoder.encode(securityUser.getOauthId(), StandardCharsets.UTF_8))
                    .queryParam("status", "REGISTER")
                    .build()
                    .encode()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        } else {
            Member member = memberService.findByProviderAndOauthId(
                    securityUser.getProvider(),
                    securityUser.getOauthId()
            );
            String accessToken = authTokenService.generateToken(member.getEmail(), member.getUserRole()).accessToken();
            String refreshToken = memberService.generateRefreshToken(member.getEmail());
            log.debug("Generated JWT access token: {}", accessToken);
            log.debug("Generated JWT refresh token: {}", refreshToken);

            Map<String, Object> roleData = new HashMap<>();
            roleData.put("role", member.getUserRole());

            String encodedRoleData = URLEncoder.encode(Ut.json.toString(roleData), StandardCharsets.UTF_8);

            Cookie roleCookie = new Cookie("role", encodedRoleData);
            roleCookie.setSecure(true);
            roleCookie.setPath("/");
            response.addCookie(roleCookie);

            Cookie accessTokenCookie = new Cookie("access_token", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            String redirectUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                    .queryParam("status", "SUCCESS")
                    .build()
                    .toUriString();

            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);

            response.sendRedirect(redirectUrl);
        }
    }
}