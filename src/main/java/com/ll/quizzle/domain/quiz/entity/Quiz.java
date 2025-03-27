package com.ll.quizzle.domain.quiz.entity;

import com.ll.quizzle.domain.quiz.enums.AnswerType;
import com.ll.quizzle.domain.quiz.enums.Difficulty;
import com.ll.quizzle.domain.quiz.enums.MainCategory;
import com.ll.quizzle.domain.quiz.enums.SubCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // PK

    @Enumerated(EnumType.STRING)
    @Column(name = "main_category", nullable = false)
    private MainCategory mainCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false)
    private SubCategory subCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private AnswerType answerType;

    @Column(name = "problem_count", nullable = false)
    private int problemCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private Difficulty difficulty;

    @Builder
    public Quiz(MainCategory mainCategory,
                SubCategory subCategory,
                AnswerType answerType,
                int problemCount,
                Difficulty difficulty) {
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.answerType = answerType;
        this.problemCount = problemCount;
        this.difficulty = difficulty;
    }


}
