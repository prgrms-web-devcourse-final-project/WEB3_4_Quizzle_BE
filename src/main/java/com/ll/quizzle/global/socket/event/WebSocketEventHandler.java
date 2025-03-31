package com.ll.quizzle.global.socket.event;

import com.ll.quizzle.domain.member.service.MemberService;
import com.ll.quizzle.global.socket.service.WebSocketNotificationService;
import com.ll.quizzle.global.socket.session.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventHandler {

    private final WebSocketSessionManager sessionManager;
    private final MemberService memberService;
    private final WebSocketNotificationService notificationService;


    @EventListener
    public void onSessionConnectEvent(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        
        if (sessionAttributes != null && sessionAttributes.containsKey("email")) {
            String email = (String) sessionAttributes.get("email");
            String accessToken = (String) sessionAttributes.get("accessToken");
            String sessionId = (String) sessionAttributes.get("sessionId");
            Long expiryTime = (Long) sessionAttributes.get("tokenExpiryTime");
            
            log.debug("세션 연결 이벤트: 사용자={}, 세션={}", email, sessionId);
            
            sessionManager.registerSession(email, sessionId, accessToken, expiryTime);
            
            if (!memberService.verifyToken(accessToken)) {
                log.debug("세션 연결 시 토큰 만료 감지: 사용자={}", email);
                notificationService.sendTokenExpiredNotification(email);
            }
        }
    }


    @EventListener
    public void onSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        
        if (accessor.getUser() != null) {
            String email = accessor.getUser().getName();
            log.debug("세션 종료 이벤트: 사용자={}, 세션={}", email, sessionId);
            sessionManager.removeSession(email, sessionId);
        }
    }
} 