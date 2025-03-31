package com.ll.quizzle.global.socket.dto.request;

import com.ll.quizzle.global.socket.type.MessageType;

/**
 * 클라이언트로부터 받는 채팅 메시지 요청을 위한 DTO
 */
public record ChatMessageRequest(
    MessageType type,
    String content,
    String senderId,
    String senderName,
    long timestamp,
    String roomId
) {

    public static ChatMessageRequest of(
            MessageType type,
            String content,
            String senderId,
            String senderName,
            long timestamp,
            String roomId
    ) {
        return new ChatMessageRequest(type, content, senderId, senderName, timestamp, roomId);
    }
} 