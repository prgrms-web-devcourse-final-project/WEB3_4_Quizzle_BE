package com.ll.quizzle.domain.quiz.controller;


import com.ll.quizzle.domain.quiz.dto.response.QuizResultResponse;
import com.ll.quizzle.domain.quiz.service.QuizResultService;
import com.ll.quizzle.domain.member.service.MemberExpService;
import com.ll.quizzle.global.response.RsData;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizResultController {

    private final QuizResultService quizResultService;
    private final MemberExpService memberExpService;

    public QuizResultController(QuizResultService quizResultService, MemberExpService memberExpService) {
        this.quizResultService = quizResultService;
        this.memberExpService = memberExpService;
    }

    /**
     * 최종 결과 조회 엔드포인트.
     * Redis에 저장된 제출 데이터를 기반으로 사용자별 점수를 계산하고,
     * 각 사용자의 EXP를 갱신한 후 결과를 반환합니다.
     *
     * @param roomId 퀴즈(또는 방) 아이디
     * @return 사용자별 결과 목록
     */
    @GetMapping("/{roomId}/result")
    public RsData<List<QuizResultResponse>> getQuizResults(@PathVariable("roomId") String roomId) {
        List<QuizResultResponse> results = quizResultService.getQuizResults(roomId);

        // 각 사용자에 대해 EXP 갱신 처리
        for (QuizResultResponse result : results) {
            memberExpService.updateMemberExp(result.getUserId(), result.getScore());
        }

        return RsData.success(HttpStatus.OK, results);
    }
}
