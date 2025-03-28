package com.ll.quizzle.domain.quiz.service;

import com.ll.quizzle.domain.quiz.dto.QuizSubmissionResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisQuizSubmissionService {

    private static final Duration QUIZ_TTL = Duration.ofMinutes(30);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 사용자가 제출한 답안을 채점하고, 그 결과를 Redis에 저장합니다.
     *
     * @param quizId          Redis에 저장된 퀴즈 아이디 (roomId)
     * @param questionNumber  제출한 문제 번호
     * @param submittedAnswer 사용자가 제출한 답안
     * @return QuizSubmissionResultDTO (문제 번호, 채점 결과, 정답, 메시지 포함)
     */
    public QuizSubmissionResultDTO submitAnswer(String quizId, int questionNumber, String submittedAnswer) {
        // 정답 리스트가 저장된 Redis 키 생성
        String answerListKey = "quiz:" + quizId + ":answers";

        // Redis 리스트 크기를 이용해 문제 번호 유효성 검사
        Long size = redisTemplate.opsForList().size(answerListKey);
        if (size == null || questionNumber > size.intValue() || questionNumber <= 0) {
            throw new IllegalArgumentException("유효하지 않은 문제 번호입니다.");
        }

        // 리스트에서 해당 문제의 정답을 조회 (리스트 인덱스는 0부터 시작하므로 questionNumber - 1)
        Object answerObj = redisTemplate.opsForList().index(answerListKey, questionNumber - 1);
        if (answerObj == null) {
            throw new IllegalArgumentException("해당 문제의 정답을 찾을 수 없습니다.");
        }

        // 저장된 형식은 "문제번호:정답" (예: "1:a")
        String answerStr = answerObj.toString();
        String[] parts = answerStr.split(":");
        if (parts.length != 2) {
            throw new IllegalStateException("저장된 정답 형식이 올바르지 않습니다.");
        }
        String correctAnswer = parts[1].trim().toLowerCase();
        boolean isCorrect = correctAnswer.equals(submittedAnswer.trim().toLowerCase());

        // 제출 결과를 별도의 Redis 리스트에 저장 (키 예: "quiz:{quizId}:submissions")
        String submissionKey = "quiz:" + quizId + ":submissions";
        String resultStr = isCorrect ? "correct" : "incorrect";
        String submissionEntry = questionNumber + ":" + submittedAnswer.trim().toLowerCase() + ":" + resultStr;
        redisTemplate.opsForList().rightPush(submissionKey, submissionEntry);
        redisTemplate.expire(submissionKey, QUIZ_TTL);

        String message = isCorrect ? "정답입니다." : "오답입니다.";

        return new QuizSubmissionResultDTO(questionNumber, isCorrect, correctAnswer, message);
    }
}
