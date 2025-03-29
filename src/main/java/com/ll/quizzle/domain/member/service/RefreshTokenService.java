package com.ll.quizzle.domain.member.service;


import com.ll.quizzle.domain.member.repository.RefreshTokenRepository;
import com.ll.quizzle.global.jwt.dto.JwtProperties;
import com.ll.quizzle.global.jwt.dto.RefreshToken;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.standard.util.Ut;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    void saveTokenInfo(String provider, String oauthId, String refreshToken, String accessToken) {
        try {
            String providerAndOauthId = provider + ":" + oauthId;
            log.debug("Saving token for provider:oauthId : {}", providerAndOauthId);
            RefreshToken token = new RefreshToken(providerAndOauthId, refreshToken, accessToken);
            repository.save(token);

            String key = "rt:refreshToken:" + refreshToken;
            redisTemplate.expire(key, 86400, TimeUnit.SECONDS);
            log.debug("Token saved successfully for provider:oauthId : {}", providerAndOauthId);
        } catch (Exception e) {
            throw e;
        }
    }

    String generateRefreshToken(String provider, String oauthId) {
        String providerAndOauthId = provider + ":" + oauthId;
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", providerAndOauthId);
        claims.put("role", "ROLE_MEMBER");
        claims.put("type", "refresh");
        return Ut.jwt.toString(jwtProperties, claims);
    }

    @Transactional
    RsData<String> refreshAccessToken(String refreshToken) {
        try {
            String token = refreshToken.replace("Bearer ", "").trim();

            String tokenType = Ut.jwt.getClaims(jwtProperties, token).get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                REFRESH_TOKEN_INVALID.throwServiceException();
            }

            boolean tokenExists = repository.existsByRefreshToken(token);
            log.debug("Found refresh token in repository: {}", tokenExists);

            if (!tokenExists) {
                REFRESH_TOKEN_NOT_FOUND.throwServiceException();
            }
            RefreshToken resultToken = repository.findByRefreshToken(token)
                    .orElseThrow(REFRESH_TOKEN_NOT_FOUND::throwServiceException);

            String providerAndOauthId = resultToken.getId();
            String role = Ut.jwt.getClaims(jwtProperties, token).get("role", String.class);
            String newAccessToken = Ut.jwt.toString(jwtProperties, Map.of("sub", providerAndOauthId, "role", role));

            resultToken.updateAccessToken(newAccessToken);
            repository.save(resultToken);

            return RsData.success(HttpStatus.OK, newAccessToken);
        } catch (MalformedJwtException e) {
            throw TOKEN_INVALID.throwServiceException();
        } catch (Exception e) {
            throw INTERNAL_SERVER_ERROR.throwServiceException();
        }
    }

    @Transactional
    public void removeRefreshToken(String provider, String oauthId) {
        try {
            String providerAndOauthId = provider + ":" + oauthId;
            log.debug("Attempting to remove token for provider:oauthId: {}", providerAndOauthId);
            if (repository.existsById(providerAndOauthId)) {
                repository.deleteById(providerAndOauthId);
                log.debug("Token removed successfully for provider:oauthId: {}", providerAndOauthId);
            } else {
                log.debug("No token found for provider:oauthId: {}", providerAndOauthId);
                // 토큰이 없어도 오류로 처리하지 않음
            }
        } catch (Exception e) {
            throw e;
        }
    }
}