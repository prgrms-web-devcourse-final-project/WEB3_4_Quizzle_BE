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

        List<QuizResultResponse> results = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                QuizResultResponse result = processSubmissionKey(key);
                if (result != null) {
                    results.add(result);
                }
            }
        }
        return assignRanks(results);
    }

    /**
     * 각 제출 키를 처리하여 QuizResultResponse 객체를 생성합니다.
     *
     * @param key Redis에서 조회한 제출 데이터의 키
     * @return QuizResultResponse 객체 (userId가 없으면 null 반환)
     */
    private QuizResultResponse processSubmissionKey(String key) {
        String userId = extractUserIdFromKey(key);
        if (userId == null) {
            return null;
        }
        List<Object> submissions = redisTemplate.opsForList().range(key, 0, -1);
        int score = calculateScore(submissions);
        int totalQuestions = submissions != null ? submissions.size() : 0;
        int correctCount = score / SCORE_PER_CORRECT;

        // 초기 랭크는 0으로 설정
        return new QuizResultResponse(userId, correctCount, totalQuestions, score, 0, score);
    }

    /**
     * 결과 리스트에 대해 점수를 기준으로 내림차순 정렬하고 랭킹을 부여합니다.
     *
     * @param results QuizResultResponse 리스트
     * @return 랭크가 부여된 새로운 리스트
     */
    private List<QuizResultResponse> assignRanks(List<QuizResultResponse> results) {
        List<QuizResultResponse> sortedResults = results.stream()
                .sorted(Comparator.comparingInt(QuizResultResponse::score).reversed())
                .collect(Collectors.toList());

        List<QuizResultResponse> rankedResults = new ArrayList<>();
        for (int i = 0; i < sortedResults.size(); i++) {
            QuizResultResponse oldResult = sortedResults.get(i);
            QuizResultResponse newResult = new QuizResultResponse(
                    oldResult.userId(),
                    oldResult.correctCount(),
                    oldResult.totalQuestions(),
                    oldResult.score(),
                    i + 1,
                    oldResult.exp()
            );
            rankedResults.add(newResult);
        }
        return rankedResults;
    }

    /**
     * 키 형식: quiz:{quizId}:user:{userId}:submissions 에서 userId를 추출합니다.
     *
     * @param key Redis key
     * @return userId 문자열 또는 null
     */
    private String extractUserIdFromKey(String key) {
        String[] parts = key.split(":");
        return parts.length >= 4 ? parts[3] : null;
    }

    /**
     * 제출 목록에서 정답 횟수를 계산하여 점수를 산출합니다.
     *
     * @param submissions Redis에 저장된 제출 목록
     * @return 계산된 점수
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
