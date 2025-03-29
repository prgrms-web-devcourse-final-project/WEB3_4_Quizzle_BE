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
public class WebSocketRoomController {
    private final MessageService roomService;

    @Autowired
    public WebSocketRoomController(MessageServiceFactory messageServiceFactory) {
        this.roomService = messageServiceFactory.getRoomService();
    }
    
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