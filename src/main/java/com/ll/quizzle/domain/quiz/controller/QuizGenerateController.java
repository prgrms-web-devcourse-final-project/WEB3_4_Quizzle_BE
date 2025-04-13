package com.ll.quizzle.domain.quiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizResponse;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quiz Generation", description = "퀴즈 생성 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/quiz")
public class QuizGenerateController {

    private final GPTQuizService gptQuizService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "퀴즈 생성", description = "GPT를 이용하여 퀴즈를 생성하고, 생성된 퀴즈를 Redis에 저장합니다.")
    @PostMapping("/generate")
    public ResponseEntity<String> generateQuiz(@Valid @RequestBody QuizGenerationRequest request) {
        try {
            QuizResponse response = gptQuizService.generateQuiz(request);
            RsData<QuizResponse> rsData = RsData.success(HttpStatus.OK, response);
            String jsonResponse = objectMapper.writeValueAsString(rsData);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal Server Error\"}");
        }
    }
}
