package com.ll.quizzle.global.socket.dto.request;


public record WebSocketQuizSubmitRequest(
        int questionNumber,         // 사용자가 제출한 문제 번호
        String submittedAnswer      // 사용자가 제출한 답안 (예: "a", "o" 등)
) {}