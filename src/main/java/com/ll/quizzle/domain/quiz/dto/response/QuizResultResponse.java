package com.ll.quizzle.domain.quiz.dto.response;

public record QuizResultResponse(
        String userId,
        int correctCount,
        int totalQuestions,
        int score,
        int rank,
        int exp
) {}
