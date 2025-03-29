package com.ll.quizzle.domain.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisQuizAnswerService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Duration QUIZ_TTL = Duration.ofMinutes(30);

    /**
     * 퀴즈 텍스트와 문제별 정답을 Redis에 저장합니다.
     *
     * @param quizId    저장에 사용할 퀴즈 ID (null 또는 빈 문자열이면 새로운 UUID 생성)
     * @param quizText  전체 퀴즈 텍스트
     * @param answerMap 문제 번호와 정답이 담긴 맵
     * @return 실제 저장에 사용된 퀴즈 ID
     */
    public String saveQuiz(String quizId, String quizText, Map<Integer, String> answerMap) {
        // quizId가 null이거나 빈 문자열이면 새로운 UUID 생성
        if (quizId == null || quizId.trim().isEmpty()) {
            quizId = UUID.randomUUID().toString();
        }
        String redisKey = "quiz:" + quizId;

        // 1. 퀴즈 텍스트를 해시(Hash) 자료형에 저장 (필드명: "quizText")
        Map<String, String> redisMap = new LinkedHashMap<>();
        redisMap.put("quizText", quizText);
        redisTemplate.opsForHash().putAll(redisKey, redisMap);
        redisTemplate.expire(redisKey, QUIZ_TTL);

        // 2. 문제 번호와 정답은 별도의 리스트(List)에 순서대로 저장 (예: "1:a", "2:c", ...)
        String answerListKey = redisKey + ":answers";
        answerMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(entry -> {
                    String entryValue = entry.getKey() + ":" + entry.getValue();
                    redisTemplate.opsForList().rightPush(answerListKey, entryValue);
                });
        redisTemplate.expire(answerListKey, QUIZ_TTL);

        return quizId;
    }
}
