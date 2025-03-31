package com.ll.quizzle.domain.quiz.entity;

import com.ll.quizzle.domain.quiz.enums.AnswerType;
import com.ll.quizzle.domain.quiz.enums.Difficulty;
import com.ll.quizzle.domain.quiz.enums.MainCategory;
import com.ll.quizzle.domain.quiz.enums.SubCategory;
import com.ll.quizzle.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "quiz")
public class Quiz extends BaseEntity {

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

    // 기본 생성자: JPA에서 사용 (protected)
    protected Quiz() {
    }

    // 내부 생성자: Builder가 사용
    private Quiz(MainCategory mainCategory, SubCategory subCategory, AnswerType answerType, int problemCount, Difficulty difficulty) {
        this.mainCategory = mainCategory;
        this.subCategory = subCategory;
        this.answerType = answerType;
        this.problemCount = problemCount;
        this.difficulty = difficulty;
    }

    // Getter 메서드들
    public MainCategory getMainCategory() {
        return mainCategory;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }

    public int getProblemCount() {
        return problemCount;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    // 수동으로 구현한 Builder 패턴
    public static QuizBuilder builder() {
        return new QuizBuilder();
    }

    public static class QuizBuilder {
        private MainCategory mainCategory;
        private SubCategory subCategory;
        private AnswerType answerType;
        private int problemCount;
        private Difficulty difficulty;

        public QuizBuilder mainCategory(MainCategory mainCategory) {
            this.mainCategory = mainCategory;
            return this;
        }

        public QuizBuilder subCategory(SubCategory subCategory) {
            this.subCategory = subCategory;
            return this;
        }

        public QuizBuilder answerType(AnswerType answerType) {
            this.answerType = answerType;
            return this;
        }

        public QuizBuilder problemCount(int problemCount) {
            this.problemCount = problemCount;
            return this;
        }

        public QuizBuilder difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Quiz build() {
            // 필요한 검증 로직을 추가할 수도 있습니다.
            return new Quiz(mainCategory, subCategory, answerType, problemCount, difficulty);
        }
    }
}
