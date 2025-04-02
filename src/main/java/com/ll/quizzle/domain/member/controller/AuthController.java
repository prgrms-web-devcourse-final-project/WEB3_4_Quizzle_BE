package com.ll.quizzle.domain.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ll.quizzle.domain.member.dto.request.TokenRefreshRequest;
import com.ll.quizzle.domain.member.dto.response.TokenInfoResponse;
import com.ll.quizzle.domain.member.service.AuthService;
import com.ll.quizzle.global.response.RsData;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/token-info")
    public RsData<TokenInfoResponse> getTokenInfo(
            @CookieValue(value = "access_token", required = false) String accessToken, 
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        TokenInfoResponse tokenInfo = authService.getTokenInfo(accessToken, refreshToken);
        return RsData.success(HttpStatus.OK,  tokenInfo);
    }

    @PostMapping("/token/refresh")
    public RsData<TokenInfoResponse> refreshToken(
            @RequestBody TokenRefreshRequest request,
            @CookieValue(value = "refresh_token", required = false) String cookieRefreshToken,
            HttpServletResponse response) {
        
        String refreshToken = (request != null && request.refreshToken() != null) 
                ? request.refreshToken() 
                : cookieRefreshToken;
                
        TokenInfoResponse tokenInfo = authService.refreshToken(refreshToken, response);
        return RsData.success(HttpStatus.OK, tokenInfo);
    }
} 