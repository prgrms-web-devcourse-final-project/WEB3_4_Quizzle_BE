package com.ll.quizzle.domain.quiz.dto.response;

public record QuizSubmitResponse(
        int questionNumber,         // 채점 대상 문제 번호
        boolean correct,            // 정답 여부
        String correctAnswer,       // 정답 (참고용)
        String message              // 채점 결과 메시지 (예: "정답입니다", "오답입니다")
) {
}