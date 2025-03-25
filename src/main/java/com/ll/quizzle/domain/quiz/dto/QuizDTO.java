package com.ll.quizzle.domain.quiz.dto;

import com.ll.quizzle.domain.room.type.AnswerType;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.domain.room.type.QuizCategory;
import com.ll.quizzle.domain.room.type.SubCategory;

public record QuizDTO(
        Long id,
        QuizCategory quizCategory,
        SubCategory subCategory,
        AnswerType answerType,
        int problemCount,
        Difficulty difficulty
) {
}
