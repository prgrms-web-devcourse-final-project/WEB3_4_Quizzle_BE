package com.ll.quizzle.domain.member.service;


import com.ll.quizzle.global.jwt.dto.GeneratedToken;
import com.ll.quizzle.global.jwt.dto.JwtProperties;
import com.ll.quizzle.standard.util.Ut;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthTokenService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    public GeneratedToken generateToken(String email, String role) {
        String accessToken = genAccessToken(email, role);
        String refreshToken = refreshTokenService.generateRefreshToken(email);

        refreshTokenService.saveTokenInfo(email, refreshToken, accessToken);
        return new GeneratedToken(accessToken, refreshToken);
    }

    String genAccessToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", email);
        claims.put("role", role);
        claims.put("type", "access");

        return Ut.jwt.toString(jwtProperties, claims);
    }

    boolean verifyToken(String token) {
        try {
            Claims claims = Ut.jwt.getClaims(jwtProperties, token);
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Access Token 만료됨: {}", e.getMessage());
        } catch (SignatureException e) {
            log.debug("Access Token 서명 불일치: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("Access Token 구조 이상: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("Access Token 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    String getEmail(String token) {
        return Ut.jwt.getClaims(jwtProperties, token).getSubject();
    }
}