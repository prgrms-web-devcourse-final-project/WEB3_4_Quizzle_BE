package com.ll.quizzle.domain.quiz.dto;

public record QuizSubmissionResultDTO(
        int questionNumber,
        boolean correct,
        String correctAnswer,
        String message
) {
}
