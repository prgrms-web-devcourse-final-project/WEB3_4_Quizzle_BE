package com.ll.quizzle.domain.quiz.entity;


import com.ll.quizzle.domain.room.type.AnswerType;
import com.ll.quizzle.domain.room.type.Difficulty;
import com.ll.quizzle.domain.room.type.QuizCategory;
import com.ll.quizzle.domain.room.type.SubCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 스펙상 기본 생성자 필요
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // PK

    @Enumerated(EnumType.STRING)
    @Column(name = "quiz_category", nullable = false)
    private QuizCategory quizCategory;

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

    // Lombok Builder 사용
    @Builder
    public Quiz(QuizCategory quizCategory,
                SubCategory subCategory,
                AnswerType answerType,
                int problemCount,
                Difficulty difficulty) {
        this.quizCategory = quizCategory;
        this.subCategory = subCategory;
        this.answerType = answerType;
        this.problemCount = problemCount;
        this.difficulty = difficulty;
    }

    // 필요하다면 setter나 비즈니스 메서드를 추가로 정의
    public void updateProblemCount(int problemCount) {
        this.problemCount = problemCount;
    }
}
