//package com.ll.quizzle.domain.quiz.parser;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
//import com.ll.quizzle.domain.quiz.dto.response.QuizGenerationResponse;
//import com.ll.quizzle.global.exceptions.ErrorCode;
//
//import java.io.IOException;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class QuizResponseParser {
//
//    private static final Pattern QUESTION_PATTERN =
//            Pattern.compile("^(?:Question\\s+|문제\\s*)?(\\d+)\\.");
//    private static final Pattern ANSWER_PATTERN =
//            Pattern.compile("(?i)(?:정답|answer)[:：]\\s*([A-DOX])");
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public QuizGenerationResponse parse(String responseBody) {
//        try {
//            JsonNode root = objectMapper.readTree(responseBody);
//            String content = root.path("choices").get(0).path("message").path("content").asText();
//
//            StringBuilder quizText = new StringBuilder();
//            Map<Integer, String> answerMap = new LinkedHashMap<>();
//            int currentQuestion = 0;
//
//            for (String line : content.split("\\r?\\n")) {
//                Matcher qm = QUESTION_PATTERN.matcher(line.trim());
//                if (qm.find()) {
//                    currentQuestion = Integer.parseInt(qm.group(1));
//                    quizText.append(line).append("\n");
//                    continue;
//                }
//                Matcher am = ANSWER_PATTERN.matcher(line.trim());
//                if (am.find() && currentQuestion > 0) {
//                    answerMap.put(currentQuestion, am.group(1).toLowerCase());
//                    quizText.append(line).append("\n");
//                    continue;
//                }
//                quizText.append(line).append("\n");
//            }
//
//            return new QuizGenerationResponse(quizText.toString().trim(), answerMap);
//        } catch (IOException e) {
//            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
//            return null; // Unreachable
//        }
//    }
//}

package com.ll.quizzle.domain.quiz.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizGenerationResponse;
import com.ll.quizzle.global.exceptions.ErrorCode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizResponseParser {

    private static final Pattern QUESTION_PATTERN =
            Pattern.compile("^(?:Question\\s+|문제\\s*)?(\\d+)\\.");
    private static final Pattern ANSWER_PATTERN =
            Pattern.compile("(?i)(?:정답|answer)[:：]\\s*([A-DOX])");

    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizGenerationResponse parse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            Map<Integer, String> questionMap = new LinkedHashMap<>();
            Map<Integer, String> answerMap = new LinkedHashMap<>();
            int currentQuestion = 0;
            StringBuilder currentQuestionText = new StringBuilder();

            for (String line : content.split("\\r?\\n")) {
                // 질문 패턴: "1. " 또는 "문제 1." 등으로 시작하는지 확인
                Matcher qm = QUESTION_PATTERN.matcher(line.trim());
                if (qm.find()) {
                    // 이전 질문이 있다면 저장
                    if (currentQuestion != 0) {
                        questionMap.put(currentQuestion, currentQuestionText.toString().trim());
                    }
                    // 새 질문 번호 설정
                    currentQuestion = Integer.parseInt(qm.group(1));
                    currentQuestionText = new StringBuilder();
                    // 질문 번호와 점 제거하고 남은 부분만 저장
                    String questionLine = line.replaceFirst("^\\d+\\.\\s*", "");
                    currentQuestionText.append(questionLine).append("\n");
                    continue;
                }
                // 정답 패턴: "정답:" 또는 "answer:" 등
                Matcher am = ANSWER_PATTERN.matcher(line.trim());
                if (am.find() && currentQuestion > 0) {
                    answerMap.put(currentQuestion, am.group(1).toLowerCase());
                    // 정답 문구는 질문 텍스트에 포함하지 않음
                    continue;
                }
                // 기타 라인은 현재 질문 텍스트에 추가
                if (currentQuestion != 0) {
                    currentQuestionText.append(line).append("\n");
                }
            }
            // 마지막 질문 저장
            if (currentQuestion != 0) {
                questionMap.put(currentQuestion, currentQuestionText.toString().trim());
            }

            return new QuizGenerationResponse(questionMap, answerMap);
        } catch (IOException e) {
            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
            return null; // Unreachable
        }
    }
}