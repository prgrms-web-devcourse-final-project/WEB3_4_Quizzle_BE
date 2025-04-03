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

    public String saveQuiz(String quizId, Map<Integer, String> quizTextMap, Map<Integer, String> answerMap) {
        log.info("saveQuiz 메소드 시작: quizId={}, quizTextMap={}, answerMap={}", quizId, quizTextMap, answerMap);
        if (quizId == null || quizId.trim().isEmpty()) {
            quizId = UUID.randomUUID().toString();
            log.info("quizId가 비어있어 새로운 UUID 생성: {}", quizId);
        }
        String redisKey = String.format("quiz:%s", quizId);

        // 문제별로 저장할 리스트의 key 생성
        String questionListKey = String.format("%s:questions", redisKey);
        // Map의 key값을 오름차순으로 정렬하여 각 문제를 리스트에 저장
        quizTextMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(entry -> {
                    // 저장 시 "번호: 질문내용" 형식
                    String questionEntry = String.format("%d: %s\n", entry.getKey(), entry.getValue().trim());
                    redisTemplate.opsForList().rightPush(questionListKey, questionEntry);
                    log.info("질문 리스트에 추가: key={}, entryValue={}", questionListKey, questionEntry);
                });
        redisTemplate.expire(questionListKey, QUIZ_TTL);

        // 정답은 그대로 저장
        String answerListKey = String.format("%s:answers", redisKey);
        answerMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(entry -> {
                    String entryValue = String.format("%d:%s", entry.getKey(), entry.getValue());
                    redisTemplate.opsForList().rightPush(answerListKey, entryValue);
                    log.info("정답 리스트에 추가: key={}, entryValue={}", answerListKey, entryValue);
                });
        redisTemplate.expire(answerListKey, QUIZ_TTL);

        log.info("saveQuiz 메소드 종료: 최종 quizId={}", quizId);
        return quizId;
    }

}