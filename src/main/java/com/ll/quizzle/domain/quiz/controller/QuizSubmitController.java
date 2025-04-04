package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.request.QuizSubmitRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizSubmitResponse;
import com.ll.quizzle.domain.quiz.service.RedisQuizSubmissionService;
import com.ll.quizzle.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quiz Submission", description = "퀴즈 제출 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/quiz")
public class QuizSubmitController {

    private final RedisQuizSubmissionService redisQuizSubmissionService;

    @Operation(summary = "퀴즈 답안 제출", description = "사용자 퀴즈 답안을 제출합니다.")
    @PostMapping("/{quizId}/submit")
    public RsData<QuizSubmitResponse> submitAnswer(
            @PathVariable("quizId") String quizId,
            @RequestBody QuizSubmitRequest submitRequest,
            @Parameter(description = "사용자 아이디", required = true) @RequestParam String userId) {
        QuizSubmitResponse result = redisQuizSubmissionService.submitAnswer(
                quizId,
                userId,
                submitRequest.questionNumber(),
                submitRequest.submittedAnswer()
        );
        return RsData.success(HttpStatus.OK, result);
    }
}

