package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.response.QuizResultResponse;
import com.ll.quizzle.domain.quiz.service.QuizResultService;
import com.ll.quizzle.domain.member.service.MemberExpService;
import com.ll.quizzle.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/quiz")
public class QuizResultController {

    private final QuizResultService quizResultService;
    private final MemberExpService memberExpService;

    @GetMapping("/{roomId}/result")
    public RsData<List<QuizResultResponse>> getQuizResults(@PathVariable("roomId") String roomId) {
        List<QuizResultResponse> results = quizResultService.getQuizResults(roomId);

        // 각 사용자에 대해 EXP 갱신 처리
        results.forEach(result -> memberExpService.updateMemberExp(result.userId(), result.score()));

        return RsData.success(HttpStatus.OK, results);
    }
}
