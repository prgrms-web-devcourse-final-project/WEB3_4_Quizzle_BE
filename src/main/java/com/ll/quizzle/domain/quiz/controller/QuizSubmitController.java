package com.ll.quizzle.domain.quiz.controller;

import com.ll.quizzle.domain.quiz.dto.QuizSubmissionResultDTO;
import com.ll.quizzle.domain.quiz.dto.QuizSubmitRequestDTO;
import com.ll.quizzle.domain.quiz.dto.QuizSubmitResponseDTO;
import com.ll.quizzle.domain.quiz.service.RedisQuizSubmissionService;
import com.ll.quizzle.global.exceptions.ErrorCode;
import com.ll.quizzle.global.response.RsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizSubmitController {

    @Autowired
    private RedisQuizSubmissionService redisQuizSubmissionService;

    /**
     * 사용자가 문제를 풀고 제출할 때 호출되는 API 엔드포인트.
     *
     * @param roomId        퀴즈(또는 방) 아이디 (Redis 키의 일부로 사용)
     * @param submitRequest 제출 정보 (문제 번호, 제출 답안)
     * @return 채점 결과와 관련 정보를 담은 응답 DTO
     */
    @PostMapping("/{roomId}/submit")
    public RsData<QuizSubmitResponseDTO> submitAnswer(@PathVariable("roomId") String roomId,
                                                      @RequestBody QuizSubmitRequestDTO submitRequest) {
        try {
            QuizSubmissionResultDTO result = redisQuizSubmissionService.submitAnswer(
                    roomId,
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
        } catch (Exception e) {
            // 예외 처리: 실제 운영 환경에서는 적절한 에러 응답 또는 로깅을 추가하세요.
            ErrorCode.INTERNAL_SERVER_ERROR.throwServiceException(e);
            return null; // 도달하지 않음
        }
    }
}
