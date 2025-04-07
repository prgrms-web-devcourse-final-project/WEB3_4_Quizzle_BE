package com.ll.quizzle.domain.room.service;

import com.ll.quizzle.domain.member.entity.Member;
import com.ll.quizzle.domain.member.repository.MemberRepository;
import com.ll.quizzle.domain.quiz.service.QuizParticipantService;
import com.ll.quizzle.domain.room.dto.request.RoomCreateRequest;
import com.ll.quizzle.domain.room.dto.response.RoomResponse;
import com.ll.quizzle.domain.room.entity.Room;
import com.ll.quizzle.domain.room.repository.RoomRepository;
import com.ll.quizzle.domain.room.type.RoomStatus;
import com.ll.quizzle.global.exceptions.ServiceException;
import com.ll.quizzle.global.socket.core.MessageService;
import com.ll.quizzle.global.socket.core.MessageServiceFactory;
import com.ll.quizzle.global.socket.dto.response.WebSocketRoomMessageResponse;
import com.ll.quizzle.global.socket.type.RoomMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.ll.quizzle.global.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final MessageServiceFactory messageServiceFactory;
    private final RoomBlacklistService blacklistService;
    private final QuizParticipantService quizParticipantService;


    @Transactional
    public RoomResponse createRoom(Long ownerId, RoomCreateRequest request) {
        Member owner = memberRepository.findById(ownerId)
                .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
                
        Room room = Room.builder()
                .title(request.title())
                .owner(owner)
                .capacity(request.capacity())
                .difficulty(request.difficulty())
                .mainCategory(request.mainCategory())
                .subCategory(request.subCategory())
                .password(request.isPrivate() ? request.password() : null)
                .build();
                
        Room savedRoom = roomRepository.save(room);
        return RoomResponse.from(savedRoom);
    }
    
    public List<RoomResponse> getActiveRooms() {
        List<Room> rooms = roomRepository.findByStatusNot(RoomStatus.FINISHED);
        return rooms.stream()
                .map(RoomResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void joinRoom(Long roomId, Long memberId, String password) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        if (blacklistService.isBlacklisted(roomId, memberId)) {
            throw ROOM_ENTRY_RESTRICTED.throwServiceException();
        }
                
        if (!room.validatePassword(password)) {
            throw INVALID_PASSWORD.throwServiceException();
        }
        
        if (room.isFull()) {
            ROOM_IS_FULL.throwServiceException();
        }
        
        if (!room.hasPlayer(memberId)) {
            room.addPlayer(memberId);

            quizParticipantService.registerParticipant(room.getId().toString(), memberId.toString());

            MessageService messageService = messageServiceFactory.getRoomService();
            String nickName = memberRepository.findById(memberId)
                    .map(Member::getNickname)
                    .orElse("Unknown");
            WebSocketRoomMessageResponse message = WebSocketRoomMessageResponse.of(
                RoomMessageType.JOIN,
                null,
                memberId.toString(),
                nickName,
                nickName,
                System.currentTimeMillis(),
                roomId.toString()
            );
            messageService.send("/topic/room/" + roomId, message);
        }
    }
    
    @Transactional
    public void leaveRoom(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
                
        boolean isOwner = room.isOwner(memberId);
        
        room.removePlayer(memberId);
        
        MessageService roomService = messageServiceFactory.getRoomService();
        WebSocketRoomMessageResponse message;
        
        if (isOwner) {
            if (room.getPlayers().isEmpty()) {
                roomRepository.delete(room);
                message = WebSocketRoomMessageResponse.of(
                        RoomMessageType.SYSTEM,
                        "방장이 퇴장하여 방이 삭제되었습니다.",
                        null,
                        "SYSTEM",
                        "SYSTEM",
                        System.currentTimeMillis(),
                        roomId.toString()
                );
            } else {
                Long newOwnerId = room.getPlayers().iterator().next();
                Member newOwner = memberRepository.findById(newOwnerId)
                        .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
                
                room.changeOwner(newOwner);
                
                message = WebSocketRoomMessageResponse.of(
                        RoomMessageType.SYSTEM,
                        member.getNickname() + "님이 퇴장하여 " + newOwner.getNickname() + "님이 새로운 방장이 되었습니다.",
                        null,
                        "SYSTEM",
                        "SYSTEM",
                        System.currentTimeMillis(),
                        roomId.toString()
                );
            }
            roomService.send("/topic/room/" + roomId, message);
        } else {
            message = WebSocketRoomMessageResponse.of(
                    RoomMessageType.LEAVE,
                    member.getNickname() + "님이 퇴장하셨습니다.",
                    null,
                    memberId.toString(),
                    member.getNickname(),
                    System.currentTimeMillis(),
                    roomId.toString()
            );
            roomService.send("/topic/room/" + roomId, message);
        }
    }
    
    @Transactional
    public void toggleReady(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MEMBER_NOT_FOUND::throwServiceException);
                
        if (room.hasPlayer(memberId)) {
            if (room.getReadyPlayers().contains(memberId)) {
                room.playerUnready(memberId);
                sendReadyMessage(room, member, false);
            } else {
                room.playerReady(memberId);
                sendReadyMessage(room, member, true);
            }
        }
    }
    
    private void sendReadyMessage(Room room, Member member, boolean isReady) {
        MessageService roomService = messageServiceFactory.getRoomService();
        WebSocketRoomMessageResponse message = WebSocketRoomMessageResponse.of(
                isReady ? RoomMessageType.READY : RoomMessageType.UNREADY,
                member.getNickname() + "님이 " + (isReady ? "준비" : "준비 해제") + "하셨습니다.",
                null,
                member.getId().toString(),
                member.getNickname(),
                System.currentTimeMillis(),
                room.getId().toString()
        );
        roomService.send("/topic/room/" + room.getId(), message);
    }
    
    @Transactional
    public void startGame(Long roomId, Long memberId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        if (!room.isOwner(memberId)) {
            throw NOT_ROOM_OWNER.throwServiceException();
        }
        
        if (!room.isAllPlayersReady()) {
            throw NOT_ALL_PLAYERS_READY.throwServiceException();
        }
        
        room.startGame();
        
        MessageService roomService = messageServiceFactory.getRoomService();
        WebSocketRoomMessageResponse message = WebSocketRoomMessageResponse.of(
                RoomMessageType.GAME_START,
                "게임이 시작되었습니다!",
                null,
                "SYSTEM",
                "SYSTEM",
                System.currentTimeMillis(),
                roomId.toString()
        );
        roomService.send("/topic/room/" + roomId, message);
    }
    
    @Transactional
    public void endGame(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(ROOM_NOT_FOUND::throwServiceException);
                
        room.endGame();
        
        MessageService roomService = messageServiceFactory.getRoomService();
        WebSocketRoomMessageResponse message = WebSocketRoomMessageResponse.of(
                RoomMessageType.GAME_END,
                "게임이 종료되었습니다.",
                null,
                "SYSTEM",
                "SYSTEM",
                System.currentTimeMillis(),
                roomId.toString()
        );
        roomService.send("/topic/room/" + roomId, message);
    }
}
