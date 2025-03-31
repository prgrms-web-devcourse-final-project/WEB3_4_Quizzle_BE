package com.ll.quizzle.domain.quiz.service;

import com.ll.quizzle.domain.quiz.dto.response.QuizResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuizResultService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis에 저장된 사용자별 제출 데이터를 기반으로 결과를 산출합니다.
     * 제출 데이터의 키는 "quiz:{quizId}:user:{userId}:submissions" 형태입니다.
     *
     * @param quizId 퀴즈(또는 방)의 아이디
     * @return 각 사용자별 결과 리스트
     */
    public List<QuizResultResponse> getQuizResults(String quizId) {
        // Redis에서 사용자별 제출 키 검색
        String pattern = String.format("quiz:%s:user:*:submissions", quizId);
        Set<String> keys = redisTemplate.keys(pattern);

        List<QuizResultResponse> resultList = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                // 키 형식: quiz:{quizId}:user:{userId}:submissions
                String[] parts = key.split(":");
                if (parts.length < 4) continue;
                String userId = parts[3];

                List<Object> submissions = redisTemplate.opsForList().range(key, 0, -1);
                int correctCount = 0;
                int totalQuestions = submissions != null ? submissions.size() : 0;

                if (submissions != null) {
                    for (Object submission : submissions) {
                        // 저장된 제출 형식: "questionNumber:submittedAnswer:result"
                        String entry = submission.toString();
                        String[] tokens = entry.split(":");
                        if (tokens.length == 3 && "correct".equals(tokens[2])) {
                            correctCount++;
                        }
                    }
                }

                // 예시: 점수 = correctCount * 10, EXP는 점수와 동일하게 설정
                int score = correctCount * 10;
                int exp = score;

                QuizResultResponse response = new QuizResultResponse(userId, correctCount, totalQuestions, score, 0, exp);
                resultList.add(response);
            }
        }

        // 점수를 기준으로 내림차순 정렬 후 랭킹 부여
        resultList.sort(Comparator.comparingInt(QuizResultResponse::getScore).reversed());
        for (int i = 0; i < resultList.size(); i++) {
            resultList.get(i).setRank(i + 1);
        }

        return resultList;
    }
}
