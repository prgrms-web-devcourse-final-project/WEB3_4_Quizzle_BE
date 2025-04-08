package com.ll.quizzle.global.socket.dto.response;

import com.ll.quizzle.global.socket.type.RoomMessageType;

public record WebSocketQuizSubmitResponse(
        RoomMessageType type,      // 메시지 타입 (예: ANSWER_SUBMIT)
        int questionNumber,        // 채점 대상 문제 번호
        boolean correct,           // 정답 여부
        String correctAnswer,      // 정답 (참고용)
        String message,            // 결과 메시지 (예: "정답입니다", "오답입니다")
        String memberId,           // 내부 식별자 (예: 데이터베이스의 primary key)
        String nickname,           // 사용자에게 보여질 닉네임
        boolean isSubmitted,       // 제출 완료 여부
        long timestamp,            // 메시지 전송 시각 (epoch millis)
        String quizId              // 해당 퀴즈의 ID
) {}
