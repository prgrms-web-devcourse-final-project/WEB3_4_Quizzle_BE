package com.ll.quizzle.domain.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisQuizAnswerService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Duration QUIZ_TTL = Duration.ofMinutes(30);

    public String saveQuizAnswers(Map<Integer, String> answerMap) {
        String quizId = UUID.randomUUID().toString();
        String redisKey = "quiz:" + quizId;
        redisTemplate.opsForHash().putAll(redisKey, answerMap);
        redisTemplate.expire(redisKey, QUIZ_TTL);
        return quizId;
    }
}