package com.ll.quizzle.global.socket.dto.request;

import com.ll.quizzle.global.socket.type.RoomMessageType;

/**
 * 클라이언트로부터 받는 게임 상태, 준비 상태 등의 메시지 요청을 위한 DTO
 */
public record RoomMessageRequest(
    RoomMessageType type,
    String content,
    String data,
    String senderId,
    String senderName,
    long timestamp,
    String roomId
) {

    public static RoomMessageRequest of(
            RoomMessageType type,
            String content,
            String data,
            String senderId,
            String senderName,
            long timestamp,
            String roomId
    ) {
        return new RoomMessageRequest(type, content, data, senderId, senderName, timestamp, roomId);
    }
} 