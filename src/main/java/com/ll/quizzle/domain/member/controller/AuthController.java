package com.ll.quizzle.domain.member.controller;

import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.global.response.RsData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;

    // 토큰 정보 응답을 위한 DTO 추가
    @Getter
    @AllArgsConstructor
    static class TokenInfoResponse {
        private String accessToken;
        private Long accessTokenExpiryTime;
        private String refreshToken;
    }
    
    // 토큰 갱신 요청 DTO
    @Getter
    static class TokenRefreshRequest {
        private String refreshToken;
        
        // Jackson 용 기본 생성자
        public TokenRefreshRequest() {
        }
        
        public TokenRefreshRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    /**
     * 현재 로그인한 사용자의 토큰 정보를 조회합니다.
     */
    @GetMapping("/token-info")
    public RsData<TokenInfoResponse> getTokenInfo(
            @CookieValue(value = "access_token", required = false) String accessToken, 
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        
        if (accessToken == null || refreshToken == null) {
            return new RsData<>(HttpStatus.BAD_REQUEST, "토큰 정보가 없습니다.", null);
        }
        
        try {
            // 액세스 토큰 만료 시간 조회
            Long expiryTime = memberService.getTokenExpiryTime(accessToken);
            
            return new RsData<>(
                    HttpStatus.OK,
                    "토큰 정보 조회 성공",
                    new TokenInfoResponse(accessToken, expiryTime, refreshToken)
            );
        } catch (Exception e) {
            log.error("토큰 정보 조회 중 오류 발생", e);
            return new RsData<>(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "토큰 정보 조회 실패: " + e.getMessage(),
                    null
            );
        }
    }
    
    /**
     * 리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다.
     */
    @PostMapping("/token/refresh")
    public RsData<TokenInfoResponse> refreshToken(
            @RequestBody TokenRefreshRequest request,
            @CookieValue(value = "refresh_token", required = false) String cookieRefreshToken,
            HttpServletResponse response) {
        
        // 요청 본문의 리프레시 토큰이 없으면 쿠키에서 시도
        String refreshToken = (request != null && request.getRefreshToken() != null) 
                ? request.getRefreshToken() 
                : cookieRefreshToken;
        
        if (refreshToken == null) {
            return new RsData<>(HttpStatus.BAD_REQUEST, "리프레시 토큰이 없습니다.", null);
        }
        
        try {
            log.debug("토큰 갱신 시도 - 리프레시 토큰: {}", refreshToken);
            
            // 액세스 토큰 갱신
            RsData<String> refreshResult = memberService.refreshAccessToken(refreshToken);
            
            if (refreshResult.isFail()) {
                return new RsData<>(
                        refreshResult.getResultCode(), 
                        refreshResult.getMsg(), 
                        null
                );
            }
            
            // 새 액세스 토큰
            String newAccessToken = refreshResult.getData();
            Long expiryTime = memberService.getTokenExpiryTime(newAccessToken);
            
            // 중요: 응답에 쿠키 추가 - 브라우저가 새 토큰으로 업데이트하도록
            Cookie accessTokenCookie = new Cookie("access_token", newAccessToken);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setHttpOnly(true);
            response.addCookie(accessTokenCookie);
            
            return new RsData<>(
                    HttpStatus.OK,
                    "토큰 갱신 성공",
                    new TokenInfoResponse(newAccessToken, expiryTime, refreshToken)
            );
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            return new RsData<>(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "토큰 갱신 실패: " + e.getMessage(),
                    null
            );
        }
    }
} 