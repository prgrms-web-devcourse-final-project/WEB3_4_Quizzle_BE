package com.ll.quizzle.global.socket.controller;

import com.ll.quizzle.global.socket.dto.request.WebSocketQuizSubmitRequest;
import com.ll.quizzle.global.socket.dto.response.WebSocketQuizSubmitResponse;
import com.ll.quizzle.global.socket.service.quiz.RedisQuizSubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class WebSocketQuizController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisQuizSubmissionService quizSubmissionService;

    public WebSocketQuizController(SimpMessagingTemplate messagingTemplate,
                                   RedisQuizSubmissionService quizSubmissionService) {
        this.messagingTemplate = messagingTemplate;
        this.quizSubmissionService = quizSubmissionService;
    }

    @MessageMapping("/quiz/{quizId}/submit")
    public void handleQuizSubmission(@DestinationVariable String quizId,
                                     @Payload WebSocketQuizSubmitRequest submitRequest,
                                     SimpMessageHeaderAccessor headerAccessor) {
        String senderId = headerAccessor.getUser() != null
                ? headerAccessor.getUser().getName()
                : "unknown";
        log.debug("퀴즈 제출 요청 수신 - quizId: {}, 전송자: {}, 요청 내용: {}",
                quizId, senderId, submitRequest);

        // 제출 처리를 서비스로 위임하고, WebSocket 전용 응답 DTO를 반환받음.
        WebSocketQuizSubmitResponse response = quizSubmissionService.submitAnswer(
                quizId,
                senderId,
                submitRequest.questionNumber(),
                submitRequest.submittedAnswer()
        );

        messagingTemplate.convertAndSend("/topic/quiz/" + quizId + "/updates", response);
    }
}
