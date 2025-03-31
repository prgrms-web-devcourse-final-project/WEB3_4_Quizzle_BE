package com.ll.quizzle.domain.quiz.dto;

import com.ll.quizzle.domain.quiz.enums.AnswerType;
import com.ll.quizzle.domain.quiz.enums.Difficulty;
import com.ll.quizzle.domain.quiz.enums.MainCategory;
import com.ll.quizzle.domain.quiz.enums.SubCategory;

public record QuizDTO(
        Long id,
        MainCategory mainCategory,
        SubCategory subCategory,
        AnswerType answerType,
        int problemCount,
        Difficulty difficulty
) {
}
