package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.QuizDTO;
import com.ll.quizzle.domain.quiz.dto.QuizGenerationResponseDTO;
import com.ll.quizzle.domain.quiz.dto.QuizResponseDTO;
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
            String quizId = redisQuizAnswerService.saveQuiz(providedQuizId, result.getQuizText(), result.getAnswerMap());

            QuizResponseDTO response = new QuizResponseDTO();
            response.setQuizId(quizId);
            response.setQuizText(result.getQuizText());
            response.setAnswerKey(result.getAnswerMap());

            return RsData.success(HttpStatus.OK, response);
        } catch (Exception e) {
            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
            return null; // unreachable
        }
    }


}
