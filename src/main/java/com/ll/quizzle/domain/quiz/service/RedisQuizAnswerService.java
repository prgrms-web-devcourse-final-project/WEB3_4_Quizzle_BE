package com.ll.quizzle.domain.quiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisQuizAnswerService {

    private static final Logger log = LoggerFactory.getLogger(RedisQuizAnswerService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final Duration QUIZ_TTL = Duration.ofMinutes(30);

    public String saveQuiz(String quizId, String quizText, Map<Integer, String> answerMap) {
        log.info("saveQuiz 메소드 시작: quizId={}, quizText={}, answerMap={}", quizId, quizText, answerMap);

        // quizId가 null이거나 빈 문자열이면 새로운 UUID 생성
        if (quizId == null || quizId.trim().isEmpty()) {
            quizId = UUID.randomUUID().toString();
            log.info("quizId가 비어있어 새로운 UUID 생성: {}", quizId);
        }
        String redisKey = String.format("quiz:%s", quizId);

        // 1. 퀴즈 텍스트를 해시(Hash) 자료형에 저장 (필드명: "quizText")
        Map<String, String> redisMap = new LinkedHashMap<>();
        redisMap.put("quizText", quizText);
        redisTemplate.opsForHash().putAll(redisKey, redisMap);
        redisTemplate.expire(redisKey, QUIZ_TTL);
        log.info("Redis 해시에 퀴즈 텍스트 저장 완료: key={}, value={}", redisKey, redisMap);

        // 2. 문제 번호와 정답은 별도의 리스트(List)에 순서대로 저장 (예: "1:a", "2:c", ...)
        String answerListKey = String.format("%s:answers", redisKey);
        answerMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(entry -> {
                    String entryValue = String.format("%d:%s", entry.getKey(), entry.getValue());
                    redisTemplate.opsForList().rightPush(answerListKey, entryValue);
                    log.info("정답 리스트에 추가: key={}, entryValue={}", answerListKey, entryValue);
                });
        redisTemplate.expire(answerListKey, QUIZ_TTL);
        log.info("Redis 리스트에 정답 저장 완료: key={}", answerListKey);

        log.info("saveQuiz 메소드 종료: 최종 quizId={}", quizId);
        return quizId;
    }
}
