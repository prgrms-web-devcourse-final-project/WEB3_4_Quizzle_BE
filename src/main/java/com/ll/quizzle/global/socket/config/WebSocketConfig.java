package com.ll.quizzle.global.socket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 및 STOMP 설정 클래스
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.websocket.endpoint:/ws}")
    private String endpoint;
    
    @Value("${spring.websocket.allowed-origins:*}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // topic 부분은 topic 된 API의 구독자에게 브로드 캐스트 메시지를 전달합니다.
        // queue 부분은 user와 혼합해서 사용도 가능하고, queue 자체로도 상황에 맞게 사용을 할 수 있습니다.
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트 -> 서버로 메시지를 보낼 때 사용하는 접두사 입니다.
        registry.setApplicationDestinationPrefixes("/app");

        // 개인 메시지를 위한 접두사 입니다. (보통 queue와 혼합하여 사용합니다.)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS(); // WebSocket을 지원하지 않는 브라우저를 위해 추가
    }
} 