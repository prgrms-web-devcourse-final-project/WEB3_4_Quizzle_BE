package com.ll.quizzle.global.socket.dto.response;

import com.ll.quizzle.global.socket.type.MessageType;

/**
 * 클라이언트에게 전송되는 채팅 메시지 응답을 위한 DTO
 */
public record ChatMessageResponse(
    MessageType type,
    String content,
    String senderId,
    String senderName,
    long timestamp,
    String roomId
) {

    public static ChatMessageResponse of(
            MessageType type,
            String content,
            String senderId,
            String senderName,
            long timestamp,
            String roomId
    ) {
        return new ChatMessageResponse(type, content, senderId, senderName, timestamp, roomId);
    }
} 