package com.ll.quizzle.global.socket.core;

import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.socket.config.MessageServiceProperties;
import com.ll.quizzle.global.socket.service.redis.RedisMessageService;
import com.ll.quizzle.global.socket.service.stomp.StompMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.ll.quizzle.global.exceptions.ErrorCode.WEBSOCKET_UNSUPPORTED_PROVIDER;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageServiceFactory {

    private final StompMessageService stompMessageService;
    private final RedisMessageService redisMessageService;
    private final MessageServiceProperties properties;

    public MessageService getDefaultService() {
        log.debug("기본 메시징 서비스 프로바이더: {}", properties.getProvider());
        return getService(properties.getProvider());
    }

    public MessageService getRoomService() {
        log.debug("룸 메시징 서비스 프로바이더: {}", properties.getRoom().getProvider());
        return getService(properties.getRoom().getProvider());
    }

    public MessageService getChatService() {
        log.debug("채팅 메시징 서비스 프로바이더: {}", properties.getChat().getProvider());
        return getService(properties.getChat().getProvider());
    }
    

    public MessageService getService(String provider) {
        return switch (provider.toLowerCase()) {
            case MessageServiceConstants.PROVIDER_STOMP -> stompMessageService;
            case MessageServiceConstants.PROVIDER_REDIS -> redisMessageService;
            default -> throw WEBSOCKET_UNSUPPORTED_PROVIDER.throwServiceException();
        };
    }
} 