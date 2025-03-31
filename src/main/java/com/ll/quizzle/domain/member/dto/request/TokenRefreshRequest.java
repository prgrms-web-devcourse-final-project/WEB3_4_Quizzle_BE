package com.ll.quizzle.domain.member.dto.request;

/**
 * 클라이언트로부터 리프레시 토큰을 받기 위한 DTO
 */
public record TokenRefreshRequest(
    String refreshToken
) {

    public static TokenRefreshRequest of(String refreshToken) {
        return new TokenRefreshRequest(refreshToken);
    }
} 