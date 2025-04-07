package com.ll.quizzle.global.socket.session;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹 소켓 세션 관리 전략 (추후 확장성을 고려)
 * 예로들어 새로운 이벤트 세션 즉, 카프카, RabbitMQ 등으로 대체할 경우를 대비
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionRegistry {

    @Qualifier("inMemoryWebSocketSessionManager")
    private final WebSocketSessionManager inMemorySessionManager;

    @Qualifier("redisWebSocketSessionManager")
    private final WebSocketSessionManager redisSessionManager;

    /**
     * SuppressWarnings 은 불명확해서 붙이기 싫은데 IDE 쪽에서 Value 가 불리언보다 먼저 타입 지정이되어 오류가 발생함
     * final 필드를 빼면 오류가 발생하진 않지만, RequiredArgsConstructor 가 작동하지 않을거라 일단 SuppressWarnings 처리
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Value("${app.websocket.use-distributed-sessions:false}")
    private final boolean useDistributedSessions;
    
    public WebSocketSessionManager getSessionManager() {
        if (useDistributedSessions) {
            log.debug("Redis 기반 세션 매니저 사용");
            return redisSessionManager;
        } else {
            log.debug("InMemory 기반 세션 매니저 사용");
            return inMemorySessionManager;
        }
    }
} 