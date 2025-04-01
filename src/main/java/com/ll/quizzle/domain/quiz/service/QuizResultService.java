package com.ll.quizzle.domain.quiz.service;

import com.ll.quizzle.domain.quiz.dto.response.QuizResultResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizResultService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final int SCORE_PER_CORRECT = 10;

    public QuizResultService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Redis에 저장된 제출 데이터를 기반으로 사용자별 결과를 산출합니다.
     *
     * @param quizId 퀴즈(또는 방)의 아이디
     * @return 각 사용자별 결과 리스트
     */
    public List<QuizResultResponse> getQuizResults(String quizId) {
        String pattern = String.format("quiz:%s:user:*:submissions", quizId);
        Set<String> keys = redisTemplate.keys(pattern);

        List<QuizResultResponse> resultList = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                String userId = extractUserIdFromKey(key);
                if (userId == null) continue;

                List<Object> submissions = redisTemplate.opsForList().range(key, 0, -1);
                int score = calculateScore(submissions);
                int totalQuestions = submissions != null ? submissions.size() : 0;
                int correctCount = score / SCORE_PER_CORRECT;

                QuizResultResponse response = new QuizResultResponse(userId, correctCount, totalQuestions, score, 0, score);
                resultList.add(response);
            }
        }

        // 점수를 기준으로 내림차순 정렬 후 랭킹 부여
        resultList = resultList.stream()
                .sorted(Comparator.comparingInt(QuizResultResponse::getScore).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setRank(i + 1);
        }

        return resultList;
    }

    /**
     * 키 형식: quiz:{quizId}:user:{userId}:submissions 에서 userId 추출
     */
    private String extractUserIdFromKey(String key) {
        String[] parts = key.split(":");
        return parts.length >= 4 ? parts[3] : null;
    }

    /**
     * 제출 목록에서 정답 횟수를 계산하여 점수를 산출
     */
    private int calculateScore(List<Object> submissions) {
        int correctCount = 0;
        if (submissions != null) {
            for (Object submission : submissions) {
                String[] tokens = submission.toString().split(":");
                if (tokens.length == 3 && "correct".equals(tokens[2])) {
                    correctCount++;
                }
            }
        }
        return correctCount * SCORE_PER_CORRECT;
    }
}
