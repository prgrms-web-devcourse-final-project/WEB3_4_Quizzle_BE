package com.ll.quizzle.global.socket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 DTO
 * 현재는 룸,로비 내 채팅에 사용됩니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private MessageType type;

    private String content;

    private String senderId;

    private String senderName;

    /**
     * 채팅 히스토리 기록 및 UI 표시 등 여러가지 방면으로 확장하기 위해 추가
     */
    private long timestamp;

    private String roomId;

    public enum MessageType {
        CHAT,           // 일반 채팅
        JOIN,           // 입장 메시지
        LEAVE,          // 퇴장 메시지
        SYSTEM,         // 시스템 메시지
        WHISPER         // 귓속말
    }
} 