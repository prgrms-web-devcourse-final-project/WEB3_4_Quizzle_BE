package com.ll.quizzle.global.socket.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @MessageMapping("/lobby/users")
    public void handleLobbyUsersRequest(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        String username = Objects.requireNonNull(headerAccessor.getUser()).getName();
        log.debug("로비 접속자 목록 요청: {}, 사용자: {}", message, username);
        
        try {
            Map<String, Map<String, SessionInfo>> activeSessions = sessionRegistry.getSessionManager().getActiveUserSessions();
            List<Map<String, Object>> activeUsers = convertToUsersList(activeSessions);

            String usersJson = objectMapper.writeValueAsString(activeUsers);
            roomService.send("/topic/lobby/users", usersJson);
            
            log.debug("접속자 목록 요청에 응답 전송 완료: {} 명", activeUsers.size());
        } catch (Exception e) {
            log.error("접속자 목록 요청 처리 중 오류: {}", e.getMessage(), e);
        }
    }
    
    private List<Map<String, Object>> convertToUsersList(Map<String, Map<String, SessionInfo>> activeSessions) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, SessionInfo>> entry : activeSessions.entrySet()) {
            String email = entry.getKey();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", email);

            memberService.findByEmail(email).ifPresent(member -> {
                userInfo.put("nickname", member.getNickname());
                userInfo.put("id", member.getId());
            });

            List<String> sessionIds = new ArrayList<>(entry.getValue().keySet());
            userInfo.put("sessions", sessionIds);
            userInfo.put("lastActive", System.currentTimeMillis());
            userInfo.put("status", "online");

            result.add(userInfo);
        }

        return result;
    }
} 