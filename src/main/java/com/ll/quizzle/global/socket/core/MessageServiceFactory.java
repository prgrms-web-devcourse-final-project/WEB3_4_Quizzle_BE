package com.ll.quizzle.global.socket.core;


import com.ll.quizzle.global.socket.service.redis.RedisMessageService;
import com.ll.quizzle.global.socket.service.stomp.StompMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 설정을 함에 따라 적절한 메시지들의 구현체를 갈아끼워 넣을 수 있습니다.
 * 이 부분은 추후 확장성을 고려해서 만들었으며, 단일 책임의 원칙, 인터페이스 분리 원칙을 준수하여 작성했습니다.
 * 각 기능들을 전부 말씀드리기에는 시간이 부족하고, 한 번 이 내용들을 검색하셔서 찾아보시면 될 것 같습니다.
 * 따로 필요한 내용들의 주석은 붙여두겠습니다.
 */
@Component
@RequiredArgsConstructor
public class MessageServiceFactory {

    private final StompMessageService stompMessageService; // WebSocket/STOMP 기반 구현체 DI
    private final RedisMessageService redisMessageService; // Redis PUB/SUB 기반 메시징 구현체 DI

    // 기본 메시징 프로바이더 설정 (이 기본 프로바이더는 로비 부분에서 사용하셔도 됩니다.)
    @Value("${quizzle.messaging.provider:stomp}")
    private String defaultProvider;
    
    // 룸 전용 설정 (STOMP)
    @Value("${quizzle.messaging.room.provider:stomp}")
    private String roomProvider;
    
    // 채팅 전용 설정 (Redis PUB/SUB)
    @Value("${quizzle.messaging.chat.provider:redis}")
    private String chatProvider;


    public MessageService getDefaultService() {
        return getService(defaultProvider);
    }

    public MessageService getRoomService() {
        return getService(roomProvider);
    }

    public MessageService getChatService() {
        return getService(chatProvider);
    }
    
    /**
     * 지정된 프로바이더의 메시지 서비스 구현체를 반환합니다.
     * 핸들러 어댑터 기능 정도로 이해하시면 될 것 같습니다.
     */
    public MessageService getService(String provider) {
        return switch (provider.toLowerCase()) {
            case "stomp" -> stompMessageService;
            case "redis" -> redisMessageService;
            default -> throw new IllegalArgumentException("지원하지 않는 메시징 프로바이더: " + provider);
        };
    }
} 