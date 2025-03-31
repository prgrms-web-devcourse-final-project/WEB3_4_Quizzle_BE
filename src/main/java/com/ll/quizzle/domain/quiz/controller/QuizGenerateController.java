package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizGenerationResponse;
import com.ll.quizzle.domain.quiz.dto.response.QuizResponse;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.domain.quiz.service.RedisQuizAnswerService;
import com.ll.quizzle.global.response.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizGenerateController {

    private final GPTQuizService gptQuizService;
    private final RedisQuizAnswerService redisQuizAnswerService;

    public QuizGenerateController(GPTQuizService gptQuizService, RedisQuizAnswerService redisQuizAnswerService) {
        this.gptQuizService = gptQuizService;
        this.redisQuizAnswerService = redisQuizAnswerService;
    }

    @PostMapping("/generate")
    public RsData<QuizResponse> generateQuiz(@RequestBody QuizGenerationRequest request) {
        String quizId = UUID.randomUUID().toString();

        // GPTQuizService를 통해 퀴즈 생성 결과를 받아옴
        QuizGenerationResponse generationResponse = gptQuizService.generateQuiz(request);

        // 생성된 퀴즈 정보를 Redis에 저장
        redisQuizAnswerService.saveQuiz(quizId, generationResponse.quizText(), generationResponse.answerMap());

        QuizResponse response = new QuizResponse(quizId, generationResponse.quizText(), generationResponse.answerMap());
        return RsData.success(HttpStatus.OK, response);
    }
}
