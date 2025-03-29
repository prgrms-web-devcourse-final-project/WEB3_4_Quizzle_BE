package com.ll.quizzle.global.socket.config;

import com.ll.quizzle.global.socket.interceptor.StompChannelInterceptor;
import com.ll.quizzle.global.socket.interceptor.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.websocket.endpoint:/ws}")
    private String endpoint;
    
    @Value("${spring.websocket.allowed-origins:*}")
    private String[] allowedOrigins;
    
    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    private final StompChannelInterceptor channelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2); // 하트비트용 1개, 토큰 검증용 1개
        taskScheduler.setThreadNamePrefix("ws-scheduler-");
        taskScheduler.initialize();

        registry.enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(taskScheduler)
                .setHeartbeatValue(new long[]{10000, 10000});

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS()
                .setWebSocketEnabled(true)
                .setInterceptors(handshakeInterceptor);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
    }
}
