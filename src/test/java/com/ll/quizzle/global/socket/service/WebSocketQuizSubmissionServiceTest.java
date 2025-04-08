package com.ll.quizzle.domain.quiz.service;

import com.ll.quizzle.global.socket.dto.response.WebSocketQuizSubmitResponse;
import com.ll.quizzle.global.socket.service.quiz.RedisQuizSubmissionService;
import com.ll.quizzle.global.socket.type.RoomMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisQuizSubmissionServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOps;

    @Mock
    private SetOperations<String, Object> setOps;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @InjectMocks
    private RedisQuizSubmissionService quizSubmissionService;

    private final String quizId = "quiz1";
    private final String userId = "user1";
    private final int questionNumber = 1;
    private final String submittedAnswer = "a";
    private final String answerKey = "quiz:" + quizId + ":answers";
    private final String currentQuestionKey = "quiz:" + quizId + ":currentQuestion";
    private final String submissionKey = String.format("quiz:%s:user:%s:submissions", quizId, userId);
    private final String submittedSetKey = String.format("quiz:%s:submitted:%d", quizId, questionNumber);
    private final String participantsKey = String.format("quiz:%s:participants", quizId);

    @BeforeEach
    void setUp() {
        // 각 Operations을 목객체로 설정
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("정상 제출 흐름 - 올바른 WebSocketQuizSubmitResponse 반환")
    void testValidSubmissionFlow() {
        // 전체 문제 수 1, 정답 리스트에 "1:a"가 저장되어 있다고 가정
        when(listOps.size(eq(answerKey))).thenReturn(1L);
        when(listOps.index(eq(answerKey), eq((long) questionNumber - 1))).thenReturn("1:a");
        // 현재 활성 문제 번호가 없으므로 null -> 서비스에서 1로 초기화
        when(valueOps.get(eq(currentQuestionKey))).thenReturn(null);
        // 중복 제출 검사: 빈 리스트 반환
        when(listOps.range(eq(submissionKey), anyLong(), anyLong())).thenReturn(Collections.emptyList());
        // 제출 Set에서는 아직 제출되지 않음
        when(setOps.isMember(eq(submittedSetKey), eq(userId))).thenReturn(false);
        // 참가자 수가 2명이고, 제출 Set에 있는 수가 2명 -> 이벤트 발행 대상
        when(setOps.size(eq(participantsKey))).thenReturn(2L);
        when(setOps.size(eq(submittedSetKey))).thenReturn(2L);

        WebSocketQuizSubmitResponse response = quizSubmissionService.submitAnswer(
                quizId, userId, questionNumber, submittedAnswer
        );

        // 채점 결과 검증 (정답 "a" 제출했으므로 정답, 메시지는 "정답입니다.")
        assertThat(response).isNotNull();
        assertThat(response.type()).isEqualTo(RoomMessageType.ANSWER_SUBMIT);
        assertThat(response.questionNumber()).isEqualTo(questionNumber);
        assertThat(response.correct()).isTrue();
        assertThat(response.correctAnswer()).isEqualTo("a");
        assertThat(response.message()).isEqualTo("정답입니다.");
        assertThat(response.senderId()).isEqualTo(userId);
        assertThat(response.senderName()).isEqualTo(userId);
        assertThat(response.isSubmitted()).isTrue();
        assertThat(response.quizId()).isEqualTo(quizId);

        // 모든 참가자가 제출되었으므로 convertAndSend()가 호출되어 "quizEnd" 이벤트가 발행되어야 함
        ArgumentCaptor<String> channelCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(redisTemplate, times(1))
                .convertAndSend(channelCaptor.capture(), eventCaptor.capture());
        assertThat(channelCaptor.getValue()).isEqualTo(String.format("quiz:%s:notifications", quizId));
        assertThat(eventCaptor.getValue()).isEqualTo("quizEnd");
    }

}
