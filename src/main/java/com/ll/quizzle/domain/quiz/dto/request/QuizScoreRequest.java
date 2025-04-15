package com.ll.quizzle.domain.quiz.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public record QuizScoreRequest(
        int score
) {}