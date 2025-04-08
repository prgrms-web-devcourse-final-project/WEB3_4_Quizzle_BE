package com.ll.quizzle.domain.member.controller;

import com.ll.quizzle.domain.member.dto.response.OAuth2InfoResponse;
import com.ll.quizzle.domain.member.service.OAuth2Service;
import com.ll.quizzle.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "OAuth2Controller", description = "OAuth2 관련 API")
public class OAuth2Controller {
    private final OAuth2Service oAuth2Service;


    /**
     * OAuth 정보 확인용 (테스트를 위해 일단 보류)
     *
     * @param accessToken  the access token
     * @param refreshToken the refresh token
     * @param status       the status (SUCCESS)
     *
     * @return OAuth2InfoResponse
     */
    @GetMapping("/oauth2/callback")
    @Operation(summary = "oauth 정보 확인용", description = "OAuth2 인증 정보 확인 API입니다. Status에 SUCCESS가 아닌 경우, 인증 실패로 간주합니다.")
    public RsData<OAuth2InfoResponse> callback(
            @CookieValue(value = "access_token", required = false) String accessToken,
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            @RequestParam String status
    ) {
        return oAuth2Service.OAuthInfoToResponseData(accessToken, refreshToken, status);
    }
}