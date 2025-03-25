package com.ll.quizzle.domain.quiz.dto;


import java.util.Map;

/**
 * Controller에서 클라이언트에게 반환할 최종 응답 DTO입니다.
 * 이 클래스는 생성된 퀴즈 텍스트, 문제별 정답 매핑(answerKey), 그리고 Redis에 저장된 퀴즈 식별자(quizId)를 포함합니다.
 */
public class QuizResponseDTO {
    // 클라이언트에 반환할 생성된 퀴즈 텍스트
    private String quizText;

    // 각 문제 번호와 정답을 매핑한 Map (문제 번호 -> 정답)
    // QuizGenerationResponseDTO의 answerMap과 동일한 데이터를 담습니다.
    private Map<Integer, String> answerKey;

    // Redis 등에 저장된 퀴즈의 고유 식별자 (예: UUID)
    private String quizId;

    // 기본 생성자
    public QuizResponseDTO() {}

    // 퀴즈 텍스트 반환
    public String getQuizText() {
        return quizText;
    }

    // 퀴즈 텍스트 설정
    public void setQuizText(String quizText) {
        this.quizText = quizText;
    }

    // 정답 매핑(answerKey) 반환
    public Map<Integer, String> getAnswerKey() {
        return answerKey;
    }

    // 정답 매핑(answerKey) 설정
    public void setAnswerKey(Map<Integer, String> answerKey) {
        this.answerKey = answerKey;
    }

    // 퀴즈 식별자 반환
    public String getQuizId() {
        return quizId;
    }

    // 퀴즈 식별자 설정
    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }
}