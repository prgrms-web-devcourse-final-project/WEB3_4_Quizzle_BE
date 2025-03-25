package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.QuizDTO;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.domain.quiz.service.RedisQuizAnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<QuizResponse> generateQuiz(@RequestBody QuizDTO quizDTO) {
        try {
            QuizGenerationResult result = gptQuizService.generateQuiz(quizDTO);
            String quizId = redisQuizAnswerService.saveQuizAnswers(result.getAnswerMap());

            QuizResponse response = new QuizResponse();
            response.setQuizText(result.getQuizText());
            response.setAnswerKey(result.getAnswerMap());
            response.setQuizId(quizId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}