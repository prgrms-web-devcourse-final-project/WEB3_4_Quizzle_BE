package com.ll.quizzle.global.socket.dto.response;

import com.ll.quizzle.global.socket.type.RoomMessageType;

public record WebSocketQuizSubmitResponse(
        RoomMessageType type,      // 메시지 타입 (예: ANSWER_SUBMIT)
        int questionNumber,        // 채점 대상 문제 번호
        boolean correct,           // 정답 여부
        String correctAnswer,      // 정답 (참고용)
        String message,            // 결과 메시지 (예: "정답입니다", "오답입니다")
        String senderId,           // 제출한 사용자 ID
        String senderName,         // 제출한 사용자 이름
        boolean isSubmitted,       // 제출 완료 여부
        long timestamp,            // 메시지 전송 시각 (epoch millis)
        String quizId              // 해당 방의 ID
) {}