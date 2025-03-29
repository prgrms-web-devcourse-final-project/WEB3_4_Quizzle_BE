package com.ll.quizzle.global.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.ll.quizzle.global.exceptions.ErrorCode.OAUTH_LOGIN_FAILED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        // 상세 에러 로깅
        log.error("OAuth2 인증 실패: {}", exception.getMessage());
        log.error("예외 클래스: {}", exception.getClass().getName());
        log.error("예외 상세 정보:", exception);

        // 요청 정보 로깅
        log.error("요청 URL: {}", request.getRequestURL());
        log.error("요청 메서드: {}", request.getMethod());
        log.error("요청 파라미터: {}", request.getParameterMap());
        log.error("요청 헤더 Authorization: {}", request.getHeader("Authorization"));

        // 세션 정보 확인
        log.error("세션 ID: {}", request.getSession().getId());
        log.error("세션 생성 시간: {}", request.getSession().getCreationTime());

        // 클라이언트에게 상세 정보 제공
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", OAUTH_LOGIN_FAILED.getMessage());
        errorResponse.put("error", exception.getMessage());
        errorResponse.put("errorType", exception.getClass().getSimpleName());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(OAUTH_LOGIN_FAILED));
    }
} 