package com.ll.quizzle.global.socket.interceptor;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.socket.security.WebSocketSecurityService;
import com.ll.quizzle.standard.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final MemberService memberService;
    private final WebSocketSecurityService securityService;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, 
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.debug("WebSocket 요청 타입 불일치: {}", request.getClass().getSimpleName());
            setErrorResponse(response, WEBSOCKET_INVALID_REQUEST_TYPE);
            return false;
        }

        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
        
        if (httpServletRequest.getCookies() == null) {
            log.debug("WebSocket 연결 시도 - 쿠키 없음");
            setErrorResponse(response, WEBSOCKET_COOKIE_NOT_FOUND);
            return false;
        }
        
        Optional<Cookie> accessTokenCookie = CookieUtil.getCookie(httpServletRequest, "access_token");
        if (accessTokenCookie.isEmpty()) {
            log.debug("WebSocket 연결 시도 - 액세스 토큰 없음");
            setErrorResponse(response, WEBSOCKET_ACCESS_TOKEN_NOT_FOUND);
            return false;
        }
        
        String accessToken = accessTokenCookie.get().getValue();
        log.debug("WebSocket 연결 시도 - 액세스 토큰 발견");

        try {
            String email = memberService.extractEmailIfValid(accessToken);
            Member member = memberService.findByEmail(email)
                    .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
            
            Long tokenExpiryTime = memberService.getTokenExpiryTime(accessToken);
            
            attributes.put("email", email);
            attributes.put("memberId", member.getId());
            attributes.put("accessToken", accessToken);
            attributes.put("tokenExpiryTime", tokenExpiryTime);
            attributes.put("sessionId", httpServletRequest.getSession().getId());
            
            String sessionData = email + ":" + member.getId() + ":" + tokenExpiryTime;
            String signature = securityService.generateSignature(sessionData);
            attributes.put("sessionSignature", signature);
            
            log.debug("WebSocket 연결 성공 - 사용자: {}", email);
            return true;
        } catch (Exception e) {
            log.error("WebSocket 토큰 검증 실패: {}", e.getMessage());
            setErrorResponse(response, WEBSOCKET_TOKEN_VALIDATION_FAILED);
            return false;
        }
    }

    private void setErrorResponse(ServerHttpResponse response, ErrorCode errorCode) {
        response.setStatusCode(errorCode.getHttpStatus());
        response.getHeaders().add("X-WebSocket-Error", errorCode.getMessage());
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                              @NonNull WebSocketHandler wsHandler, Exception exception) {
        // 후크 메서드 입니다. 추후에 로그 같은것들 추가할 예정 입니다.
    }
} 