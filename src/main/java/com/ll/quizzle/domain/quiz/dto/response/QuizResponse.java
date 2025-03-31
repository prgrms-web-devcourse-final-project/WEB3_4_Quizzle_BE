package com.ll.quizzle.domain.quiz.dto.generation;

import java.util.Map;

/**
 * Controller에서 클라이언트에게 반환할 최종 응답 DTO
 */
public record QuizResponse(
        String quizId,
        String quizText,
        Map<Integer, String> answerKey
) {
}
