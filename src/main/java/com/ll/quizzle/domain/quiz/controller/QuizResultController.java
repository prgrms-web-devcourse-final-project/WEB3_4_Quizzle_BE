package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.response.QuizResultResponse;
import com.ll.quizzle.domain.quiz.service.QuizResultService;
import com.ll.quizzle.domain.member.service.MemberExpService;
import com.ll.quizzle.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quiz Result", description = "퀴즈 결과 조회 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/quiz")
public class QuizResultController {

    private final QuizResultService quizResultService;
    private final MemberExpService memberExpService;

    @Operation(summary = "퀴즈 결과 조회", description = "특정 퀴즈 결과를 조회하고, 각 사용자에 대해 EXP를 갱신합니다.")
    @GetMapping("/{quizId}/result")
    public RsData<List<QuizResultResponse>> getQuizResults(@PathVariable("quizId") String quizId) {

        List<QuizResultResponse> results = quizResultService.getQuizResults(quizId);
        results.forEach(result -> memberExpService.updateMemberExp(result.userId(), result.score()));

        return RsData.success(HttpStatus.OK, results);
    }
}
