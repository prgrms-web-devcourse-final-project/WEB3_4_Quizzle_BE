package com.ll.quizzle.domain.member.controller;

import com.ll.quizzle.domain.member.dto.request.TokenRefreshRequest;
import com.ll.quizzle.domain.member.dto.response.TokenInfoResponse;
import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.global.jwt.dto.JwtProperties;
import com.ll.quizzle.global.response.RsData;
import com.ll.quizzle.standard.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final MemberService memberService;
    private final JwtProperties jwtProperties;

    @GetMapping("/token-info")
    public RsData<TokenInfoResponse> getTokenInfo(
            @CookieValue(value = "access_token", required = false) String accessToken, 
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        
        if (accessToken == null || refreshToken == null) {
            TOKEN_INFO_NOT_FOUND.throwServiceException();
        }
        
        try {
            Long expiryTime = memberService.getTokenExpiryTime(accessToken);
            
            return new RsData<>(
                    HttpStatus.OK,
                    "토큰 정보 조회 성공",
                    TokenInfoResponse.of(accessToken, expiryTime, refreshToken)
            );
        } catch (Exception e) {
            log.error("토큰 정보 조회 중 오류 발생", e);
            throw INTERNAL_SERVER_ERROR.throwServiceException(e);
        }
    }
    

    @PostMapping("/token/refresh")
    public RsData<TokenInfoResponse> refreshToken(
            @RequestBody TokenRefreshRequest request,
            @CookieValue(value = "refresh_token", required = false) String cookieRefreshToken,
            HttpServletResponse response) {
        
        String refreshToken = (request != null && request.refreshToken() != null) 
                ? request.refreshToken() 
                : cookieRefreshToken;
        
        if (refreshToken == null) {
            REFRESH_TOKEN_NOT_FOUND.throwServiceException();
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
            
            CookieUtil.addCookie(response, "access_token", newAccessToken, (int) jwtProperties.getAccessTokenExpiration(), true, true);
            
            return new RsData<>(
                    HttpStatus.OK,
                    "토큰 갱신 성공",
                    TokenInfoResponse.of(newAccessToken, expiryTime, refreshToken)
            );
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            throw INTERNAL_SERVER_ERROR.throwServiceException(e);
        }
    }
} 