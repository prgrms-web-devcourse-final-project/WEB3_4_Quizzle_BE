package com.ll.quizzle.domain.member.service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.global.jwt.dto.GeneratedToken;
import com.ll.quizzle.global.jwt.dto.JwtProperties;
import com.ll.quizzle.global.request.Rq;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.global.security.oauth2.repository.OAuthRepository;
import com.ll.quizzle.standard.util.Ut;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final OAuthRepository oAuthRepository;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, String> redisTemplate;
    private final AuthTokenService authTokenService;
    private final JwtProperties jwtProperties;
    private final Rq rq;

    private static final String LOGOUT_PREFIX = "LOGOUT:";

    @Transactional(readOnly = true)
    public Member findByProviderAndOauthId(String provider, String oauthId) {
        return oAuthRepository.findByProviderAndOauthIdWithMember(provider, oauthId)
                .orElseThrow(OAUTH_NOT_FOUND::throwServiceException)
                .getMember();
    }

    public String generateRefreshToken(String provider, String oauthId) {
        return refreshTokenService.generateRefreshToken(provider, oauthId);
    }

    public String extractProviderAndOauthIdIfValid(String token) {
        if (isLoggedOut(token)) {
            TOKEN_LOGGED_OUT.throwServiceException();
        }
        if (!verifyToken(token)) {
            TOKEN_INVALID.throwServiceException();
        }
        return getProviderAndOauthIdFromToken(token);
    }

    public boolean isLoggedOut(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LOGOUT_PREFIX + token));
    }

    public boolean verifyToken(String accessToken) {
        return authTokenService.verifyToken(accessToken);
    }

    public String getProviderAndOauthIdFromToken(String token) {
        return authTokenService.getProviderAndOauthId(token);
    }

    public RsData<String> refreshAccessToken(String refreshToken) {
        return refreshTokenService.refreshAccessToken(refreshToken);
    }

    @Transactional
    public void oAuth2Login(Member member, String provider, String oauthId, HttpServletResponse response) {
        GeneratedToken tokens = authTokenService.generateToken(
                provider,
                oauthId,
                member.getUserRole()
        );

        addAuthCookies(response, tokens, member);
    }

    private void addAuthCookies(HttpServletResponse response, GeneratedToken tokens, Member member) {
        // Access Token 쿠키
        Cookie accessTokenCookie = new Cookie("access_token", tokens.accessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키
        Cookie refreshTokenCookie = new Cookie("refresh_token", tokens.refreshToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        response.addCookie(refreshTokenCookie);

        // Role 쿠키
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("role", member.getUserRole());

        Cookie roleCookie = new Cookie("role", URLEncoder.encode(Ut.json.toString(roleData), StandardCharsets.UTF_8));
        roleCookie.setPath("/");
        response.addCookie(roleCookie);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        // 액세스 토큰이 만료되었다면 리프레시 토큰으로 처리
        if (accessToken == null && refreshToken != null) {
            RsData<String> refreshResult = refreshAccessToken(refreshToken);
            if (refreshResult.isSuccess()) {
                accessToken = refreshResult.getData();
            }
        }

        if (accessToken == null) {
            UNAUTHORIZED.throwServiceException();
        }

        String providerAndOauthId = authTokenService.getProviderAndOauthId(accessToken);

        String[] parts = providerAndOauthId.split(":");
        if (parts.length != 2) {
            throw TOKEN_INVALID.throwServiceException();
        }
        String provider = parts[0];
        String oauthId = parts[1];

        // Redis에서 토큰 무효화
        redisTemplate.opsForValue().set(
                LOGOUT_PREFIX + accessToken,
                providerAndOauthId,
                jwtProperties.getAccessTokenExpiration(),
                TimeUnit.MILLISECONDS
        );

        // Refresh 토큰 삭제
        refreshTokenService.removeRefreshToken(provider, oauthId);

        deleteCookie(response);
    }

    private static void deleteCookie(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");

        Cookie roleCookie = new Cookie("role", null);
        roleCookie.setMaxAge(0);
        roleCookie.setPath("/");

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");

        Cookie oauth2AuthRequestCookie = new Cookie("oauth2_auth_request", null);
        oauth2AuthRequestCookie.setMaxAge(0);
        oauth2AuthRequestCookie.setPath("/");

        response.addCookie(accessTokenCookie);
        response.addCookie(roleCookie);
        response.addCookie(refreshTokenCookie);
        response.addCookie(oauth2AuthRequestCookie);
    }
}
