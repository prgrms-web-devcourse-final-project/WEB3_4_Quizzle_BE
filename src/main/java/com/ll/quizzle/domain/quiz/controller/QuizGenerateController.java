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
@Profile("gpt")
public class QuizGenerateController {

    private final GPTQuizService gptQuizService;
    private final RedisQuizAnswerService redisQuizAnswerService;

    public QuizGenerateController(GPTQuizService gptQuizService, RedisQuizAnswerService redisQuizAnswerService) {
        this.gptQuizService = gptQuizService;
        this.redisQuizAnswerService = redisQuizAnswerService;
    }

    @PostMapping("/generate")
    public RsData<QuizResponseDTO> generateQuiz(@RequestBody QuizDTO quizDTO) {
        // 예시: UUID를 quizId로 사용 (필요에 따라 redisQuizAnswerService 등에서 quizId를 생성할 수 있음)
        String quizId = UUID.randomUUID().toString();

        QuizGenerationResponseDTO result = gptQuizService.generateQuiz(quizDTO);
        QuizResponseDTO response = new QuizResponseDTO(quizId, result.quizText(), result.answerMap());
        return RsData.success(HttpStatus.OK, response);
    }
}