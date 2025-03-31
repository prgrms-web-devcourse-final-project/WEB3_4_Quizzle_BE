package com.ll.quizzle.global.socket.dto.response;

import com.ll.quizzle.global.socket.type.RoomMessageType;

/**
 * 클라이언트에게 전송되는 게임 상태, 준비 상태 등의 메시지 응답을 위한 DTO
 */
public record RoomMessageResponse(
    RoomMessageType type,
    String content,
    String data,
    String senderId,
    String senderName,
    long timestamp,
    String roomId
) {

    public static RoomMessageResponse of(
            RoomMessageType type,
            String content,
            String data,
            String senderId,
            String senderName,
            long timestamp,
            String roomId
    ) {
        return new RoomMessageResponse(type, content, data, senderId, senderName, timestamp, roomId);
    }
} 