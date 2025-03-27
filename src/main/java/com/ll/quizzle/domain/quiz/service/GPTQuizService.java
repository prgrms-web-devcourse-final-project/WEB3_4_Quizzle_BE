package com.ll.quizzle.domain.quiz.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.quiz.dto.QuizDTO;
import com.ll.quizzle.domain.quiz.dto.QuizGenerationResponseDTO;
import com.ll.quizzle.domain.quiz.enums.AnswerType;
import com.ll.quizzle.global.config.OpenAIConfig;
import com.ll.quizzle.global.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GPTQuizService {

    private static final Pattern QUESTION_PATTERN =
            Pattern.compile("^(?:Question\\s+|문제\\s*)?(\\d+)\\.");
    private static final Pattern ANSWER_PATTERN =
            Pattern.compile("(?i)(?:정답|answer)[:：]\\s*([A-DOX])");

    private final OpenAIConfig openAIConfig;
    private final String apiUrl;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GPTQuizService(OpenAIConfig openAIConfig,
                          @Value("${openai.api.url}") String apiUrl,
                          @Value("${openai.model}") String model) {
        this.openAIConfig = openAIConfig;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    public QuizGenerationResponseDTO generateQuiz(QuizDTO quizDTO) {
        String optionText = (quizDTO.answerType() == AnswerType.MULTIPLE_CHOICE)
                ? "a) 보기1\nb) 보기2\nc) 보기3\nd) 보기4"
                : "O 또는 X";

        String systemPrompt = String.format(
                "너는 한국어 전문 퀴즈 생성기야. **절대로 영어 사용 금지**. 인삿말이나 추가 설명 없이 오직 문제와 정답만 출력해.\n" +
                        "대분류: %s\n소분류: %s\n문제 유형: %s\n문제 수: %d\n난이도: %s\n\n각 문제 형식:\n문제 번호. 문제 내용\n%s\n정답: <정답>",
                quizDTO.mainCategory(), quizDTO.subCategory(),
                quizDTO.answerType(), quizDTO.problemCount(), quizDTO.difficulty(),
                optionText
        );

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", "퀴즈 생성")
                ),
                "temperature", 0.7
        );

        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
            return null;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(
                        new RuntimeException("OpenAI API returned status " + response.statusCode()));
            }

            String content = objectMapper.readTree(response.body())
                    .path("choices").get(0)
                    .path("message").path("content").asText();

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

        } catch (IOException | InterruptedException e) {
            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
            return null; // unreachable
        }
    }
}
