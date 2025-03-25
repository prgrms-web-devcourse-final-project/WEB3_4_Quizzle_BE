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
        registry.enableSimpleBroker("/topic", "/queue");
        
        registry.setApplicationDestinationPrefixes("/app");
        
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS(); // WebSocket을 지원하지 않는 브라우저를 위해 추가
    }
} 