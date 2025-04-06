package com.ll.quizzle.global.socket.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.ll.quizzle.global.socket.core.MessageService;
import com.ll.quizzle.global.socket.core.MessageServiceFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class WebSocketChatController {
    private final MessageService chatService;

    @Autowired
    public WebSocketChatController(MessageServiceFactory messageServiceFactory) {
        this.chatService = messageServiceFactory.getChatService();
    }
    
    @MessageMapping("/lobby/chat")
    public void handleLobbyChatMessage(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("로비 채팅 메시지 수신: {}, 사용자: {}", message, username);
        chatService.send("/topic/lobby/chat", message);
    }

    @MessageMapping("/room/chat/{roomId}")
    public void handleRoomChatMessage(
            @DestinationVariable String roomId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("방 채팅 메시지 수신: {}, 방: {}, 사용자: {}", message, roomId, username);
        chatService.send("/topic/room/chat/" + roomId, message);
    }

    @MessageMapping("/game/chat/{roomId}")
    public void handleGameChatMessage(
            @DestinationVariable String roomId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("게임 채팅 메시지 수신: {}, 방: {}, 사용자: {}", message, roomId, username);
        chatService.send("/topic/game/chat/" + roomId, message);
    }
} 