package com.ll.quizzle.domain.quiz.dto.response;


public class QuizResultResponse {
    private String userId;
    private int correctCount;
    private int totalQuestions;
    private int score;
    private int rank;
    private int exp;

    public QuizResultResponse(String userId, int correctCount, int totalQuestions, int score, int rank, int exp) {
        this.userId = userId;
        this.correctCount = correctCount;
        this.totalQuestions = totalQuestions;
        this.score = score;
        this.rank = rank;
        this.exp = exp;
    }

    // getters and setters

    public String getUserId() {
        return userId;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }

    public int getExp() {
        return exp;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
