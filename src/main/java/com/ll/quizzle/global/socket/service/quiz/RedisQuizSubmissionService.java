package com.ll.quizzle.global.socket.service.quiz;

import com.ll.quizzle.global.socket.dto.response.WebSocketQuizSubmitResponse;
import com.ll.quizzle.global.socket.type.RoomMessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisQuizSubmissionService {

    private static final Duration QUIZ_TTL = Duration.ofMinutes(30);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public WebSocketQuizSubmitResponse submitAnswer(String quizId, String userId, int questionNumber, String submittedAnswer) {
        // 정답 검증
        String answerListKey = String.format("quiz:%s:answers", quizId);
        Long totalQuestions = redisTemplate.opsForList().size(answerListKey);
        if (totalQuestions == null || questionNumber > totalQuestions.intValue() || questionNumber <= 0) {
            throw new IllegalArgumentException("유효하지 않은 문제 번호입니다.");
        }
        Object answerObj = redisTemplate.opsForList().index(answerListKey, questionNumber - 1);
        if (answerObj == null) {
            throw new IllegalArgumentException("해당 문제의 정답을 찾을 수 없습니다.");
        }
        String answerStr = answerObj.toString();
        String[] parts = answerStr.split(":");
        if (parts.length != 2) {
            throw new IllegalStateException("저장된 정답 형식이 올바르지 않습니다.");
        }
        String correctAnswer = parts[1].trim().toLowerCase();
        boolean isCorrect = correctAnswer.equals(submittedAnswer.trim().toLowerCase());

        // 현재 활성화된 문제 번호 검증
        String currentQuestionKey = String.format("quiz:%s:currentQuestion", quizId);
        Integer currentQuestion = (Integer) redisTemplate.opsForValue().get(currentQuestionKey);
        if (currentQuestion == null) {
            currentQuestion = 1;
            redisTemplate.opsForValue().set(currentQuestionKey, currentQuestion, QUIZ_TTL);
        }
        if (questionNumber != currentQuestion) {
            throw new IllegalStateException("현재 활성화된 문제에 대해서만 답안을 제출할 수 있습니다.");
        }

        //  중복 제출 방지
        String submissionKey = String.format("quiz:%s:user:%s:submissions", quizId.trim(), userId.trim());
        if (redisTemplate.opsForList().range(submissionKey, 0, -1).stream()
                .anyMatch(entry -> entry.toString().startsWith(questionNumber + ":"))) {
            throw new IllegalStateException("이미 해당 문제에 대해 제출하셨습니다.");
        }
        // 제출 기록 저장
        String resultStr = isCorrect ? "correct" : "incorrect";
        String submissionEntry = String.format("%d:%s:%s", questionNumber, submittedAnswer.trim().toLowerCase(), resultStr);
        redisTemplate.opsForList().rightPush(submissionKey, submissionEntry);
        redisTemplate.expire(submissionKey, QUIZ_TTL);

        //  문제별 제출 상태 업데이트: 제출 Set에 등록
        String submittedSetKey = String.format("quiz:%s:submitted:%d", quizId, questionNumber);
        Boolean alreadySubmitted = redisTemplate.opsForSet().isMember(submittedSetKey, userId);
        if (Boolean.TRUE.equals(alreadySubmitted)) {
            throw new IllegalStateException("이미 제출하셨습니다.");
        }
        redisTemplate.opsForSet().add(submittedSetKey, userId);
        redisTemplate.expire(submittedSetKey, QUIZ_TTL);

        // 모든 참가자 제출 여부 확인 후 이벤트 발행
        String participantsKey = String.format("quiz:%s:participants", quizId);
        Long totalParticipants = redisTemplate.opsForSet().size(participantsKey);
        Long submittedCount = redisTemplate.opsForSet().size(submittedSetKey);
        if (totalParticipants != null && submittedCount != null && totalParticipants.equals(submittedCount)) {
            String notificationChannel = String.format("quiz:%s:notifications", quizId);
            String event = (questionNumber == totalQuestions.intValue()) ? "quizEnd" : "nextQuestion";
            redisTemplate.convertAndSend(notificationChannel, event);
        }

        String resultMessage = isCorrect ? "정답입니다." : "오답입니다.";
        long timestamp = System.currentTimeMillis();

        // WebSocket 전용 DTO 생성 (이제 기존 REST API용 DTO 대신 이 DTO를 사용)
        return new WebSocketQuizSubmitResponse(
                RoomMessageType.ANSWER_SUBMIT,
                questionNumber,
                isCorrect,
                correctAnswer,
                resultMessage,
                userId,
                userId,  // 필요에 따라 실제 사용자 이름으로 변경
                true,
                timestamp,
                quizId
        );
    }
}
