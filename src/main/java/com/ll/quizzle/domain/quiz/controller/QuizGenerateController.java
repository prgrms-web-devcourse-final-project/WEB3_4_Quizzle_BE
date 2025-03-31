package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.generation.QuizDTO;
import com.ll.quizzle.domain.quiz.dto.generation.QuizGenerationResponseDTO;
import com.ll.quizzle.domain.quiz.dto.generation.QuizResponseDTO;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.domain.quiz.service.RedisQuizAnswerService;
import com.ll.quizzle.global.response.RsData;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public RsData<QuizResponseDTO> generateQuiz(@RequestBody QuizDTO quizDTO) {
        String quizId = UUID.randomUUID().toString();

        // 먼저 GPTQuizService로부터 퀴즈 생성 결과를 받아옴
        QuizGenerationResponseDTO result = gptQuizService.generateQuiz(quizDTO);

        // result를 사용해 Redis에 저장
        redisQuizAnswerService.saveQuiz(quizId, result.quizText(), result.answerMap());

        QuizResponseDTO response = new QuizResponseDTO(quizId, result.quizText(), result.answerMap());
        return RsData.success(HttpStatus.OK, response);
    }
}