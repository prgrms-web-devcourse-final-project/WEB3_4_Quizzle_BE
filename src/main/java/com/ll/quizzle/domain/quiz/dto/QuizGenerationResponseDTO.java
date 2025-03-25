package com.ll.quizzle.domain.quiz.dto;

import java.util.Map;

/**
 * DTO representing the response from the GPT quiz generation process.
 * 이 클래스는 GPT API를 통해 생성된 퀴즈 텍스트와 문제 번호별 정답 매핑을 담습니다.
 */

public class QuizGenerationResponseDTO {
    private String quizText;
    private Map<Integer, String> answerMap;

    public QuizGenerationResponseDTO() {}

    public QuizGenerationResponseDTO(String quizText, Map<Integer, String> answerMap) {
        this.quizText = quizText;
        this.answerMap = answerMap;
    }

    public String getQuizText() {
        return quizText;
    }
    public void setQuizText(String quizText) {
        this.quizText = quizText;
    }
    public Map<Integer, String> getAnswerMap() {
        return answerMap;
    }
    public void setAnswerMap(Map<Integer, String> answerMap) {
        this.answerMap = answerMap;
    }
}
