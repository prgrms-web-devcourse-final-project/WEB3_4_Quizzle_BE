package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.request.QuizSubmitRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizSubmitResponse;
import com.ll.quizzle.domain.quiz.service.RedisQuizSubmissionService;
import com.ll.quizzle.global.response.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizSubmitController {

    private final RedisQuizSubmissionService redisQuizSubmissionService;

    public QuizSubmitController(RedisQuizSubmissionService redisQuizSubmissionService) {
        this.redisQuizSubmissionService = redisQuizSubmissionService;
    }

    @PostMapping("/{roomId}/submit")
    public RsData<QuizSubmitResponse> submitAnswer(@PathVariable("roomId") String roomId,
                                                   @RequestBody QuizSubmitRequest submitRequest,
                                                   @RequestParam String userId) {
        QuizSubmitResponse result = redisQuizSubmissionService.submitAnswer(
                roomId,
                userId,
                submitRequest.questionNumber(),
                submitRequest.submittedAnswer()
        );
        return RsData.success(HttpStatus.OK, result);
    }


}
