package com.ll.quizzle.domain.quiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class QuizParticipantService {

    private static final Duration QUIZ_TTL = Duration.ofMinutes(30);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void registerParticipant(String quizId, String userId) {
        String participantsKey = String.format("quiz:%s:participants", quizId);
        redisTemplate.opsForSet().add(participantsKey, userId);
        redisTemplate.expire(participantsKey, QUIZ_TTL);
    }

    public Set<String> getParticipants(String quizId) {
        String participantsKey = String.format("quiz:%s:participants", quizId);
        return redisTemplate.opsForSet().members(participantsKey);
    }
}
