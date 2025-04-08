package com.ll.quizzle.global.socket.service;


import com.ll.quizzle.global.socket.controller.WebSocketQuizController;
import com.ll.quizzle.global.socket.dto.request.WebSocketQuizSubmitRequest;
import com.ll.quizzle.global.socket.dto.response.WebSocketQuizSubmitResponse;
import com.ll.quizzle.global.socket.service.quiz.RedisQuizSubmissionService;
import com.ll.quizzle.global.socket.type.RoomMessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketQuizControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisQuizSubmissionService quizSubmissionService;

    @InjectMocks
    private WebSocketQuizController quizController;

    @Test
    void testHandleQuizSubmission() {
        // given
        String quizId = "quiz1";
        String userId = "user1";
        int questionNumber = 1;
        String submittedAnswer = "a";

        WebSocketQuizSubmitRequest request = new WebSocketQuizSubmitRequest(
                questionNumber,
                submittedAnswer
        );

        // 생성 시점의 timestamp는 테스트 시마다 달라질 수 있으므로, 검증 시 ArgumentMatchers.anyLong() 등을 사용합니다.
        WebSocketQuizSubmitResponse expectedResponse = new WebSocketQuizSubmitResponse(
                RoomMessageType.ANSWER_SUBMIT,  // 메시지 타입
                questionNumber,
                true,                           // 채점 결과 (예시: 정답)
                "a",                            // 정답
                "정답입니다.",                  // 결과 메시지
                userId,                         // 제출한 사용자 ID
                userId,                         // 제출한 사용자 이름
                true,                           // 제출 완료 여부
                System.currentTimeMillis(),     // 타임스탬프 (테스트에서는 정확한 값 대신 anyLong()으로 검증)
                quizId
        );

        // quizSubmissionService.submitAnswer() 메서드 호출 시 expectedResponse 반환하도록 설정
        when(quizSubmissionService.submitAnswer(eq(quizId), eq(userId), eq(questionNumber), eq(submittedAnswer)))
                .thenReturn(expectedResponse);

        // SimpMessageHeaderAccessor 생성 및 사용자 정보 설정 (여기서는 lambda를 사용하여 이름을 반환)
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setUser(() -> userId);

        // when
        quizController.handleQuizSubmission(quizId, request, headerAccessor);

        // then: messagingTemplate.convertAndSend()가 올바른 구독 채널과 응답 DTO로 호출되었는지 검증
        verify(messagingTemplate).convertAndSend("/topic/quiz/" + quizId + "/updates", expectedResponse);

        // 또한, quizSubmissionService.submitAnswer() 호출 여부도 검증
        verify(quizSubmissionService).submitAnswer(eq(quizId), eq(userId), eq(questionNumber), eq(submittedAnswer));
    }
}
