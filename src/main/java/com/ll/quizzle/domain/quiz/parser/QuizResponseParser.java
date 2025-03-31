package com.ll.quizzle.domain.quiz.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.quiz.dto.generation.QuizGenerationResponseDTO;
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

    public QuizGenerationResponseDTO parse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            StringBuilder quizText = new StringBuilder();
            Map<Integer, String> answerMap = new LinkedHashMap<>();
            int currentQuestion = 0;

            for (String line : content.split("\\r?\\n")) {
                Matcher qm = QUESTION_PATTERN.matcher(line.trim());
                if (qm.find()) {
                    currentQuestion = Integer.parseInt(qm.group(1));
                    quizText.append(line).append("\n");
                    continue;
                }
                Matcher am = ANSWER_PATTERN.matcher(line.trim());
                if (am.find() && currentQuestion > 0) {
                    answerMap.put(currentQuestion, am.group(1).toLowerCase());
                    quizText.append(line).append("\n");
                    continue;
                }
                quizText.append(line).append("\n");
            }

            return new QuizGenerationResponseDTO(quizText.toString().trim(), answerMap);
        } catch (IOException e) {
            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
            return null; // Unreachable
        }
    }
}
