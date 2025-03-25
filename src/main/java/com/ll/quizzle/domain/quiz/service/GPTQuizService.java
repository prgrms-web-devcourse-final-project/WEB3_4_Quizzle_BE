package com.ll.quizzle.domain.quiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.quiz.dto.QuizDTO;
import com.ll.quizzle.domain.quiz.dto.QuizGenerationResponseDTO;
import com.ll.quizzle.global.gptconfig.OpenAIConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class GPTQuizService {

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

    public QuizGenerationResponseDTO generateQuiz(QuizDTO quizDTO) throws Exception {
        // Record accessor는 get 접두어 없이 사용합니다.
        String systemPrompt = String.format(
                "Create a quiz with the following options:\n" +
                        "Quiz Category: %s\n" +
                        "Sub Category: %s\n" +
                        "Answer Type: %s\n" +
                        "Number of questions: %d\n" +
                        "Difficulty: %s\n" +
                        "For each question, include the correct answer indicated as 'Answer: <option>'.",
                quizDTO.quizCategory(),
                quizDTO.subCategory(),
                quizDTO.answerType(),
                quizDTO.problemCount(),
                quizDTO.difficulty()
        );

        // GPT API에 전달할 메시지 배열 구성
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", "Please generate the quiz."));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        // 요청 본문을 JSON 문자열로 변환
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAIConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("API call failed: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = root.path("choices").get(0).path("message").path("content").asText();

        // GPT가 생성한 텍스트에서 문제와 'Answer:' 라인을 구분하여 파싱
        StringBuilder quizTextBuilder = new StringBuilder();
        Map<Integer, String> answerMap = new LinkedHashMap<>();
        int currentQuestion = 0;
        for (String line : content.split("\n")) {
            if (line.matches("^\\d+\\.\\s.*")) { // 예: "1. What is..."
                currentQuestion++;
                quizTextBuilder.append(line).append("\n");
            } else if (line.toLowerCase().startsWith("answer:")) {
                String answer = line.split("[:\\s]+", 2)[1].trim();
                answerMap.put(currentQuestion, answer);
            } else {
                quizTextBuilder.append(line).append("\n");
            }
        }

        return new QuizGenerationResponseDTO(quizTextBuilder.toString().trim(), answerMap);
    }
}
