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
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final MemberService memberService;

    @Getter
    @AllArgsConstructor
    static class TokenInfoResponse {
        private String accessToken;
        private Long accessTokenExpiryTime;
        private String refreshToken;
    }
    
    @Getter
    static class TokenRefreshRequest {
        private String refreshToken;
        
        public TokenRefreshRequest() {
        }
        
        public TokenRefreshRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }


    @GetMapping("/token-info")
    public RsData<TokenInfoResponse> getTokenInfo(
            @CookieValue(value = "access_token", required = false) String accessToken, 
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        
        if (accessToken == null || refreshToken == null) {
            return new RsData<>(HttpStatus.BAD_REQUEST, "토큰 정보가 없습니다.", null);
        }
        
        try {
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
    

    @PostMapping("/token/refresh")
    public RsData<TokenInfoResponse> refreshToken(
            @RequestBody TokenRefreshRequest request,
            @CookieValue(value = "refresh_token", required = false) String cookieRefreshToken,
            HttpServletResponse response) {
        
        String refreshToken = (request != null && request.getRefreshToken() != null) 
                ? request.getRefreshToken() 
                : cookieRefreshToken;
        
        if (refreshToken == null) {
            return new RsData<>(HttpStatus.BAD_REQUEST, "리프레시 토큰이 없습니다.", null);
        }
        
        try {
            log.debug("토큰 갱신 시도 - 리프레시 토큰: {}", refreshToken);
            
            RsData<String> refreshResult = memberService.refreshAccessToken(refreshToken);
            
            if (refreshResult.isFail()) {
                return new RsData<>(
                        refreshResult.getResultCode(), 
                        refreshResult.getMsg(), 
                        null
                );
            }
            
            String newAccessToken = refreshResult.getData();
            Long expiryTime = memberService.getTokenExpiryTime(newAccessToken);
            
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