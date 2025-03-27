package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.QuizDTO;
import com.ll.quizzle.domain.quiz.dto.QuizGenerationResponseDTO;
import com.ll.quizzle.domain.quiz.dto.QuizResponseDTO;
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
    public ResponseEntity<QuizResponseDTO> generateQuiz(@RequestBody QuizDTO quizDTO) {
        try {
            QuizGenerationResponseDTO result = gptQuizService.generateQuiz(quizDTO);
            String providedQuizId = (quizDTO.id() != null) ? quizDTO.id().toString() : null;
            String quizId = redisQuizAnswerService.saveQuiz(providedQuizId, result.getQuizText(), result.getAnswerMap());

            QuizResponseDTO response = new QuizResponseDTO();
            response.setQuizId(quizId);
            response.setQuizText(result.getQuizText());
            response.setAnswerKey(result.getAnswerMap());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
