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
    private WebSocketRoom websocketRoom;
    private WebSocketChat websocketChat;

    @Getter
    @Setter
    public static class WebSocketRoom {
        private String provider = MessageServiceConstants.PROVIDER_STOMP;
    }

    @Getter
    @Setter
    public static class WebSocketChat {
        private String provider = MessageServiceConstants.PROVIDER_STOMP;
    }
} 