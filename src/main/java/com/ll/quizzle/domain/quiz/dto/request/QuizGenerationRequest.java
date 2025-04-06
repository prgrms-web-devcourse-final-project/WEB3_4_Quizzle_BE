package com.ll.quizzle.domain.quiz.dto.request;

import com.ll.quizzle.domain.room.type.AnswerType;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.domain.room.type.MainCategory;
import com.ll.quizzle.domain.room.type.SubCategory;


public record QuizGenerationRequest(
        MainCategory mainCategory,
        SubCategory subCategory,
        AnswerType answerType,
        int problemCount,
        Difficulty difficulty
) {
}
