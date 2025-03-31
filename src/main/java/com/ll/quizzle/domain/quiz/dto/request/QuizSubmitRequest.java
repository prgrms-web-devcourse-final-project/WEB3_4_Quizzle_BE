package com.ll.quizzle.domain.quiz.dto.submission;

public record QuizSubmitRequest(
        int questionNumber,         // 사용자가 제출한 문제 번호
        String submittedAnswer      // 사용자가 제출한 답안 (예: "a", "o" 등)
) {
}