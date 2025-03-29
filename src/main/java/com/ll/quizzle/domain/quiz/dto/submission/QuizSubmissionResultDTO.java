package com.ll.quizzle.domain.quiz.dto.submission;

public record QuizSubmissionResultDTO(
        int questionNumber,
        boolean correct,
        String correctAnswer,
        String message
) {
}
