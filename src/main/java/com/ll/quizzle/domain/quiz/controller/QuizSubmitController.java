package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.submission.QuizSubmissionResultDTO;
import com.ll.quizzle.domain.quiz.dto.submission.QuizSubmitRequestDTO;
import com.ll.quizzle.domain.quiz.dto.submission.QuizSubmitResponseDTO;
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

    /**
     * 사용자가 문제를 풀고 제출할 때 호출되는 API 엔드포인트.
     *
     * @param roomId        퀴즈(또는 방) 아이디 (Redis 키의 일부로 사용)
     * @param submitRequest 제출 정보 (문제 번호, 제출 답안)
     * @param userId        사용자 ID (요청 파라미터 또는 인증 정보에서 획득)
     * @return 채점 결과와 관련 정보를 담은 응답 DTO
     */
    @PostMapping("/{roomId}/submit")
    public RsData<QuizSubmitResponseDTO> submitAnswer(@PathVariable("roomId") String roomId,
                                                      @RequestBody QuizSubmitRequestDTO submitRequest,
                                                      @RequestParam String userId) {
        QuizSubmissionResultDTO result = redisQuizSubmissionService.submitAnswer(
                roomId,
                userId,
                submitRequest.questionNumber(),
                submitRequest.submittedAnswer()
        );

        QuizSubmitResponseDTO response = new QuizSubmitResponseDTO(
                result.questionNumber(),
                result.correct(),
                result.correctAnswer(),
                result.message()
        );
        return RsData.success(HttpStatus.OK, response);
    }
}
