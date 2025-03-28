package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.generation.QuizDTO;
import com.ll.quizzle.domain.quiz.dto.generation.QuizGenerationResponseDTO;
import com.ll.quizzle.domain.quiz.dto.generation.QuizResponseDTO;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.domain.quiz.service.RedisQuizAnswerService;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.response.RsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizGenerateController {

    @Autowired
    private GPTQuizService gptQuizService;

    @Autowired
    private RedisQuizAnswerService redisQuizAnswerService;

    @PostMapping("/generate")
    public RsData<QuizResponseDTO> generateQuiz(@RequestBody QuizDTO quizDTO) {
        try {
            QuizGenerationResponseDTO result = gptQuizService.generateQuiz(quizDTO);

            String providedQuizId = (quizDTO.id() != null) ? quizDTO.id().toString() : null;
            String quizId = redisQuizAnswerService.saveQuiz(providedQuizId, result.quizText(), result.answerMap());

            QuizResponseDTO response = new QuizResponseDTO(quizId, result.quizText(), result.answerMap());


            return RsData.success(HttpStatus.OK, response);
        } catch (Exception e) {
            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
            return null; // unreachable
        }
    }

}
