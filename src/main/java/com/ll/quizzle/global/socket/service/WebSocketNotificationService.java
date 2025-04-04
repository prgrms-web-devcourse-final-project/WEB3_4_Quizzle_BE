package com.ll.quizzle.global.socket.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebSocketNotificationService {
    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketNotificationService(@Lazy SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendTokenExpiredNotification(String email) {
        String destination = "/user/" + email + "/queue/token-expired";
        messagingTemplate.convertAndSend(destination, "토큰이 만료되었습니다. 다시 로그인해주세요.");
        log.debug("토큰 만료 알림 전송 완료 - 사용자: {}", email);
    }
} 