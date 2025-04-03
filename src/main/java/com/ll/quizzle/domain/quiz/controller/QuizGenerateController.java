package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizGenerationResponse;
import com.ll.quizzle.domain.quiz.dto.response.QuizResponse;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.domain.quiz.service.RedisQuizAnswerService;
import com.ll.quizzle.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Quiz Generation", description = "퀴즈 생성 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/quiz")
public class QuizGenerateController {

    private final GPTQuizService gptQuizService;
    private final RedisQuizAnswerService redisQuizAnswerService;

    @Operation(summary = "퀴즈 생성", description = "GPT를 이용하여 퀴즈를 생성하고, 생성된 퀴즈를 Redis에 저장합니다.")
    @PostMapping("/generate")
    public RsData<QuizResponse> generateQuiz(@RequestBody QuizGenerationRequest request) {
        String quizId = UUID.randomUUID().toString();

        QuizGenerationResponse generationResponse = gptQuizService.generateQuiz(request);

        redisQuizAnswerService.saveQuiz(quizId, generationResponse.quizText(), generationResponse.answerMap());

        QuizResponse response = new QuizResponse(quizId, generationResponse.quizText(), generationResponse.answerMap());
        return RsData.success(HttpStatus.OK, response);
    }
}
