package com.ll.quizzle.global.socket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.room.entity.Room;
import com.ll.quizzle.global.socket.core.MessageService;
import com.ll.quizzle.global.socket.core.MessageServiceFactory;
import com.ll.quizzle.global.socket.dto.response.WebSocketRoomMessageResponse;
import com.ll.quizzle.global.socket.type.RoomMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.ll.quizzle.domain.room.type.RoomStatus;
import com.ll.quizzle.global.exceptions.ErrorCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 방 관련 WebSocket 메시지 전송을 처리하는 클래스입니다.
 * 플레이어 목록 정보를 포함한 모든 메시지들은 여기서 중앙 집중적으로 관리를 하도록 설계하였습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomMessageService {

    private final ObjectMapper objectMapper;
    private final MessageServiceFactory messageServiceFactory;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;


    public void sendWithPlayersList(Room room, RoomMessageType type, String content, 
                                    String senderId, String senderName) {
        try {
            String playersData = buildPlayersListJson(room);
            sendMessage(room.getId(), type, content, playersData, senderId, senderName);
        } catch (Exception e) {
            log.error("방 메시지 전송 실패: {}", e.getMessage());
            // JSON 파싱이 실패해도 기본 메시지는 전송하게끔 처리
            sendMessage(room.getId(), type, content, null, senderId, senderName);
        }
    }

    public void sendSystemWithPlayersList(Room room, RoomMessageType type, String content) {
        sendWithPlayersList(room, type, content, "SYSTEM", "SYSTEM");
    }

    public void sendGameStart(Room room) {
        sendSystemWithPlayersList(room, RoomMessageType.GAME_START, "게임이 시작되었습니다!");
    }

    public void sendGameEnd(Room room) {
        sendSystemWithPlayersList(room, RoomMessageType.GAME_END, "게임이 종료되었습니다.");
    }

    public void sendReadyStatusChange(Room room, Member member, RoomMessageType type) {

        if (type != RoomMessageType.READY && type != RoomMessageType.UNREADY) {
            ErrorCode.INVALID_READY_MESSAGE_TYPE.throwServiceException();
        }
        
        boolean isReady = (type == RoomMessageType.READY);
        String content = member.getNickname() + "님이 " + (isReady ? "준비" : "준비 해제") + "하셨습니다.";
        
        sendWithPlayersList(room, type, content, member.getId().toString(), member.getNickname());
    }

    public void sendJoin(Room room, Member member) {
        String content = member.getNickname() + "님이 입장하셨습니다.";
        sendWithPlayersList(room, RoomMessageType.JOIN, content, 
                member.getId().toString(), member.getNickname());
    }

    public void sendLeave(Room room, Member member) {
        String content = member.getNickname() + "님이 퇴장하셨습니다.";
        sendWithPlayersList(room, RoomMessageType.LEAVE, content, 
                member.getId().toString(), member.getNickname());
    }

    public void sendOwnerChanged(Room room, Member oldOwner, Member newOwner) {
        String content = oldOwner.getNickname() + "님이 퇴장하여 " + 
                newOwner.getNickname() + "님이 새로운 방장이 되었습니다.";
        sendSystemWithPlayersList(room, RoomMessageType.SYSTEM, content);
    }

    public void sendRoomDeleted(Long roomId) {
        String content = "방장이 퇴장하여 방이 삭제되었습니다.";
        try {
            List<Map<String, Object>> emptyPlayersList = new ArrayList<>();
            String emptyPlayersJson = objectMapper.writeValueAsString(emptyPlayersList);
            
            sendMessage(roomId, RoomMessageType.SYSTEM, content, emptyPlayersJson, "SYSTEM", "SYSTEM");
        } catch (Exception e) {
            log.error("방 삭제 메시지 전송 실패: {}", e.getMessage());
            sendMessage(roomId, RoomMessageType.SYSTEM, content, null, "SYSTEM", "SYSTEM");
        }
    }


    private String buildPlayersListJson(Room room) throws JsonProcessingException {
        List<Map<String, Object>> playersList = new ArrayList<>();
        
        boolean isGameInProgress = RoomStatus.IN_GAME.equals(room.getStatus());
        String quizId = null;
        String currentRoundKey = null;
        Integer currentRound = null;
        
        if (isGameInProgress) {
            quizId = room.getId().toString();
            currentRoundKey = String.format("quiz:%s:currentRound", quizId);
            Object roundObj = redisTemplate.opsForValue().get(currentRoundKey);
            currentRound = roundObj != null ? Integer.valueOf(roundObj.toString()) : null;
            
            log.debug("진행 중인 게임 확인: 룸={}, 퀴즈ID={}, 현재라운드={}", room.getId(), quizId, currentRound);
        }
        
        for (Long playerId : room.getPlayers()) {
            Member playerMember = memberRepository.findById(playerId).orElse(null);
            if (playerMember != null) {
                Map<String, Object> playerInfo = new HashMap<>();
                playerInfo.put("id", playerMember.getId().toString());
                playerInfo.put("name", playerMember.getNickname());
                playerInfo.put("isReady", room.getReadyPlayers().contains(playerId));
                playerInfo.put("isOwner", room.isOwner(playerId));
                
                if (isGameInProgress && currentRound != null) {
                    String userId = playerMember.getId().toString();
                    String submissionKey = String.format("quiz:%s:user:%s:submissions", quizId, userId);
                    Long submissionsCount = redisTemplate.opsForList().size(submissionKey);
                    
                    boolean hasSubmitted = submissionsCount != null && submissionsCount >= currentRound;
                    playerInfo.put("isSubmitted", hasSubmitted);
                    
                    log.debug("플레이어 제출 여부: 사용자={}, 현재라운드={}, 제출여부={}", userId, currentRound, hasSubmitted);
                } else {
                    playerInfo.put("isSubmitted", false);
                }
                playersList.add(playerInfo);
            }
        }
        
        return objectMapper.writeValueAsString(playersList);
    }


    private void sendMessage(Long roomId, RoomMessageType type, String content, 
                           String data, String senderId, String senderName) {
        MessageService roomService = messageServiceFactory.getRoomService();
        WebSocketRoomMessageResponse message = WebSocketRoomMessageResponse.of(
            type,
            content,
            data,
            senderId,
            senderName,
            System.currentTimeMillis(),
            roomId.toString()
        );
        
        roomService.send("/topic/room/" + roomId, message);
    }
} 