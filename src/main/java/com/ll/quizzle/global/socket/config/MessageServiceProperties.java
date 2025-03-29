package com.ll.quizzle.global.socket.config;

import com.ll.quizzle.global.socket.core.MessageServiceConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "quizzle.messaging")
public class MessageServiceProperties {
    private String provider = MessageServiceConstants.PROVIDER_STOMP;
    
    private final Room room = new Room();
    
    private final Chat chat = new Chat();
    
    @Getter
    @Setter
    public static class Room {
        private String provider = MessageServiceConstants.PROVIDER_STOMP;
    }
    
    @Getter
    @Setter
    public static class Chat {
        private String provider = MessageServiceConstants.PROVIDER_REDIS;
    }
} 