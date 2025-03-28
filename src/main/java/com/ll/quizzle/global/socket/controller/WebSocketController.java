package com.ll.quizzle.global.socket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.global.socket.core.MessageService;
import com.ll.quizzle.global.socket.core.MessageServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Slf4j
@Controller
public class WebSocketController {
    private final MessageService roomService;
    private final MessageService chatService;

    @Autowired
    public WebSocketController(MessageServiceFactory messageServiceFactory, ObjectMapper objectMapper) {
        this.roomService = messageServiceFactory.getRoomService();
        this.chatService = messageServiceFactory.getChatService();
    }
    
    // 채팅 관련
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

    // 2. 상태 관련
    @MessageMapping("/lobby")
    public void handleLobbyMessage(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("로비 상태 메시지 수신: {}, 사용자: {}", message, username);
        roomService.send("/topic/lobby", message);
    }

    @MessageMapping("/room/{roomId}")
    public void handleRoomMessage(
            @DestinationVariable String roomId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("방 상태 메시지 수신: {}, 방: {}, 사용자: {}", message, roomId, username);
        roomService.send("/topic/room/" + roomId, message);
    }

    @MessageMapping("/game/{roomId}")
    public void handleGameMessage(
            @DestinationVariable String roomId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("게임 상태 메시지 수신: {}, 방: {}, 사용자: {}", message, roomId, username);
        roomService.send("/topic/game/" + roomId, message);
    }

    // 3. 특수 상태 관련
    @MessageMapping("/game/start/{roomId}")
    public void handleGameStart(
            @DestinationVariable String roomId,
            @Payload String message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("게임 시작 메시지 수신: {}, 방: {}, 사용자: {}", message, roomId, username);
        roomService.send("/topic/game/start/" + roomId, message);
    }
}