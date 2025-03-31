package com.ll.quizzle.domain.quiz.service;

import com.ll.quizzle.domain.quiz.dto.response.QuizSubmitResponse;
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
    public QuizSubmitResponse submitAnswer(String quizId, String userId, int questionNumber, String submittedAnswer) {
        // 정답 리스트가 저장된 Redis 키 생성 (문제의 경우는 그대로)
        String answerListKey = String.format("quiz:%s:answers", quizId);

        Long size = redisTemplate.opsForList().size(answerListKey);
        if (size == null || questionNumber > size.intValue() || questionNumber <= 0) {
            throw new IllegalArgumentException("유효하지 않은 문제 번호입니다.");
        }

        Object answerObj = redisTemplate.opsForList().index(answerListKey, questionNumber - 1);
        if (answerObj == null) {
            throw new IllegalArgumentException("해당 문제의 정답을 찾을 수 없습니다.");
        }

        String answerStr = answerObj.toString();
        String[] parts = answerStr.split(":");
        if (parts.length != 2) {
            throw new IllegalStateException("저장된 정답 형식이 올바르지 않습니다.");
        }

        String correctAnswer = parts[1].trim().toLowerCase();
        boolean isCorrect = correctAnswer.equals(submittedAnswer.trim().toLowerCase());

        // 사용자별 제출 정보를 저장하기 위한 Redis 키 생성
        String submissionKey = String.format("quiz:%s:user:%s:submissions", quizId.trim(), userId.trim());
        String resultStr = isCorrect ? "correct" : "incorrect";
        String submissionEntry = String.format("%d:%s:%s",
                questionNumber,
                submittedAnswer.trim().toLowerCase(),
                resultStr);


        redisTemplate.opsForList().rightPush(submissionKey, submissionEntry);
        redisTemplate.expire(submissionKey, QUIZ_TTL);

        String message = isCorrect ? "정답입니다." : "오답입니다.";

        return new QuizSubmitResponse(questionNumber, isCorrect, correctAnswer, message);
    }

}
