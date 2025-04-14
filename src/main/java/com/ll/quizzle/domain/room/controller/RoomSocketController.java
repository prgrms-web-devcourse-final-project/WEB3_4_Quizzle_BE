package com.ll.quizzle.domain.room.controller;

import com.ll.quizzle.domain.quiz.dto.request.QuizGenerationRequest;
import com.ll.quizzle.domain.quiz.dto.response.QuizResponse;
import com.ll.quizzle.domain.quiz.service.GPTQuizService;
import com.ll.quizzle.domain.room.dto.response.RoomResponse;
import com.ll.quizzle.domain.room.service.RoomService;
import com.ll.quizzle.domain.room.type.AnswerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RoomSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final GPTQuizService gptQuizService;
    private final RedisTemplate<String, String> redisTemplate;

    @MessageMapping("/room/{roomId}/quiz/generate")
    public void generateQuiz(@DestinationVariable String roomId, 
                             @Payload Map<String, Object> payload,
                             SimpMessageHeaderAccessor headerAccessor) {
        log.debug("퀴즈 생성 요청 - 방 ID: {}", roomId);

        Map<String, Object> startMessage = new HashMap<>();
        startMessage.put("status", "STARTED");
        startMessage.put("message", "AI가 문제를 생성하기 시작했습니다.");
        startMessage.put("progress", 10);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", startMessage);
        
        try {
            Long roomIdLong = Long.parseLong(roomId);
            RoomResponse roomResponse = roomService.getRoom(roomIdLong);
            
            if (roomResponse == null) {
                Map<String, Object> failMessage = new HashMap<>();
                failMessage.put("status", "FAILED");
                failMessage.put("message", "방 정보를 찾을 수 없습니다.");
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", failMessage);
                return;
            }
            
            Map<String, Object> progressMessage = new HashMap<>();
            progressMessage.put("status", "IN_PROGRESS");
            progressMessage.put("message", "카테고리: " + roomResponse.mainCategory() + " / " 
                    + roomResponse.subCategory() + " 관련 문제를 생성 중입니다.");
            progressMessage.put("progress", 30);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", progressMessage);
            
            CompletableFuture.runAsync(() -> {
                try {
                    QuizGenerationRequest quizRequest = new QuizGenerationRequest(
                            roomResponse.mainCategory(),
                            roomResponse.subCategory(),
                            AnswerType.MULTIPLE_CHOICE,
                            5,
                            roomResponse.difficulty(),
                            roomId
                    );
                    
                    Map<String, Object> updatingMessage = new HashMap<>();
                    updatingMessage.put("status", "IN_PROGRESS");
                    updatingMessage.put("message", "AI가 문제를 생성하고 있습니다...");
                    updatingMessage.put("progress", 50);
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", updatingMessage);
                    
                    QuizResponse quizResponse = gptQuizService.generateQuiz(quizRequest);
                    
                    Map<String, Object> processingMessage = new HashMap<>();
                    processingMessage.put("status", "IN_PROGRESS");
                    processingMessage.put("message", "생성된 문제를 처리하고 있습니다...");
                    processingMessage.put("progress", 80);
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", processingMessage);
                    
                    String roomQuizKey = String.format("room:%s:quizId", roomId);
                    redisTemplate.opsForValue().set(roomQuizKey, quizResponse.quizId(), Duration.ofMinutes(30));
                    log.debug("방 ID {}와 퀴즈 ID {} 매핑 저장 완료", roomId, quizResponse.quizId());
                    
                    Map<String, Object> completedMessage = new HashMap<>();
                    completedMessage.put("status", "COMPLETED");
                    completedMessage.put("message", "문제 생성이 완료되었습니다. 게임을 시작합니다.");
                    completedMessage.put("progress", 100);
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", completedMessage);
                    
                    Map<String, Object> gameStartMessage = new HashMap<>();
                    gameStartMessage.put("gameStatus", "IN_PROGRESS");
                    gameStartMessage.put("message", "게임이 시작되었습니다.");
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/status", gameStartMessage);
                    
                } catch (Exception e) {
                    log.error("퀴즈 생성 중 오류 발생: {}", e.getMessage(), e);
                    Map<String, Object> errorMessage = new HashMap<>();
                    errorMessage.put("status", "FAILED");
                    errorMessage.put("message", "문제 생성 중 오류가 발생했습니다: " + e.getMessage());
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", errorMessage);
                }
            });
            
        } catch (Exception e) {
            log.error("퀴즈 생성 요청 처리 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("status", "FAILED");
            errorMessage.put("message", "요청 처리 중 오류가 발생했습니다: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/quiz/generation", errorMessage);
        }
    }


    @MessageMapping("/room/{roomId}/answer")
    public void handlePlayerAnswer(@DestinationVariable String roomId,
                                   @Payload Map<String, Object> payload,
                                   SimpMessageHeaderAccessor headerAccessor) {
        log.debug("플레이어 답변 수신 - 방 ID: {}, 페이로드: {}", roomId, payload);
        
        try {
            String questionId = (String) payload.get("questionId");
            Object playerId = payload.get("playerId");
            Object answer = payload.get("answer");
            Boolean isCorrect = (Boolean) payload.get("isCorrect");
            Long timestamp = (Long) payload.get("timestamp");
            
            Map<String, Object> responseData = new HashMap<>();
            
            if (questionId != null) responseData.put("questionId", questionId);
            if (playerId != null) responseData.put("playerId", playerId);
            responseData.put("answer", answer != null ? answer : "TIMEOUT");
            if (isCorrect != null) responseData.put("isCorrect", isCorrect);
            if (timestamp != null) responseData.put("timestamp", timestamp);
            
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/answer", responseData);
            
            
        } catch (Exception e) {
            log.error("플레이어 답변 처리 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("message", "답변 처리 중 오류가 발생했습니다: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
        }
    }
    

    @MessageMapping("/room/{roomId}/question/result")
    public void handleQuestionResult(@DestinationVariable String roomId,
                                    @Payload Map<String, Object> payload,
                                    SimpMessageHeaderAccessor headerAccessor) {
        log.debug("문제 결과 요청 - 방 ID: {}, 페이로드: {}", roomId, payload);
        
        try {
            Integer questionIndex = (Integer) payload.get("questionIndex");
            Long timestamp = (Long) payload.get("timestamp");
            
            Map<String, Object> responseData = new HashMap<>();
            
            if (questionIndex != null) responseData.put("questionIndex", questionIndex);
            responseData.put("showResult", true);
            if (timestamp != null) responseData.put("timestamp", timestamp);
            
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/question/result", responseData);
            
            // 추가 로직 요기서 구현
            
        } catch (Exception e) {
            log.error("문제 결과 처리 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("message", "결과 처리 중 오류가 발생했습니다: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
        }
    }
    

    @MessageMapping("/room/{roomId}/game/start")
    public void handleGameStart(@DestinationVariable String roomId,
                               @Payload Map<String, Object> payload,
                               SimpMessageHeaderAccessor headerAccessor) {
        log.debug("게임 시작 요청 - 방 ID: {}, 페이로드: {}", roomId, payload);
        
        try {
            String roomQuizKey = String.format("room:%s:quizId", roomId);
            String quizId = redisTemplate.opsForValue().get(roomQuizKey);
            
            if (quizId == null) {
                log.error("게임 시작 실패 - 퀴즈 ID를 찾을 수 없습니다. 방 ID: {}", roomId);
                Map<String, Object> errorMessage = new HashMap<>();
                errorMessage.put("message", "게임 시작에 필요한 퀴즈 정보를 찾을 수 없습니다.");
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
                return;
            }
            
            String gameStatusKey = String.format("room:%s:gameStatus", roomId);
            String gameStatus = redisTemplate.opsForValue().get(gameStatusKey);
            
            if ("STARTED".equals(gameStatus)) {
                log.debug("게임이 이미 시작되었습니다 - 방 ID: {}", roomId);
                return;
            }
            
            redisTemplate.opsForValue().set(gameStatusKey, "STARTED", Duration.ofMinutes(30));
            
            log.info("게임 시작 - 방 ID: {}, 퀴즈 ID: {}", roomId, quizId);
            
            Map<String, Object> gameStartMessage = new HashMap<>();
            gameStartMessage.put("status", "STARTED");
            gameStartMessage.put("message", "게임이 시작되었습니다.");
            gameStartMessage.put("quizId", quizId);
            gameStartMessage.put("timestamp", System.currentTimeMillis());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/status", gameStartMessage);
            
            try {
                Thread.sleep(1500); // 1.5초 대기로 증가
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            log.info("첫 번째 문제 전송 시작 - 방 ID: {}, 퀴즈 ID: {}", roomId, quizId);
            
            CompletableFuture.runAsync(() -> {
                try {
                    String currentRoundKey = String.format("quiz:%s:currentRound", quizId);
                    redisTemplate.opsForValue().set(currentRoundKey, "0", Duration.ofMinutes(30));
                    
                    sendQuizQuestion(roomId, quizId, 0);
                    log.info("첫 번째 문제 전송 완료 - 방 ID: {}", roomId);
                } catch (Exception e) {
                    log.error("첫 번째 문제 전송 중 오류 발생: {}", e.getMessage(), e);
                }
            });
            
        } catch (Exception e) {
            log.error("게임 시작 처리 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("message", "게임 시작 중 오류가 발생했습니다: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
        }
    }
    

    @MessageMapping("/room/{roomId}/question/next")
    public void handleNextQuestion(@DestinationVariable String roomId,
                                  @Payload Map<String, Object> payload,
                                  SimpMessageHeaderAccessor headerAccessor) {
        log.debug("다음 문제 요청 - 방 ID: {}, 페이로드: {}", roomId, payload);
        
        try {
            Integer questionIndex = (Integer) payload.get("questionIndex");
            
            if (questionIndex == null) {
                log.error("다음 문제 요청 실패 - 문제 인덱스가 없습니다. 방 ID: {}", roomId);
                Map<String, Object> errorMessage = new HashMap<>();
                errorMessage.put("message", "다음 문제를 가져오는데 필요한 정보가 부족합니다.");
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
                return;
            }
            
            String roomQuizKey = String.format("room:%s:quizId", roomId);
            String quizId = redisTemplate.opsForValue().get(roomQuizKey);
            
            if (quizId == null) {
                log.error("다음 문제 요청 실패 - 퀴즈 ID를 찾을 수 없습니다. 방 ID: {}", roomId);
                Map<String, Object> errorMessage = new HashMap<>();
                errorMessage.put("message", "게임에 필요한 퀴즈 정보를 찾을 수 없습니다.");
                messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
                return;
            }
            
            int nextQuestionIndex = questionIndex + 1;
            sendQuizQuestion(roomId, quizId, nextQuestionIndex);
            
        } catch (Exception e) {
            log.error("다음 문제 처리 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("message", "다음 문제 처리 중 오류가 발생했습니다: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
        }
    }
    

    private void sendQuizQuestion(String roomId, String quizId, int questionIndex) {
        try {
            String questionListKey = String.format("quiz:%s:questions", quizId);
            Object questionObj = redisTemplate.opsForList().index(questionListKey, questionIndex);
            
            if (questionObj == null) {
                if (questionIndex >= 5) {
                    Map<String, Object> gameEndMessage = new HashMap<>();
                    gameEndMessage.put("status", "FINISHED");
                    gameEndMessage.put("message", "모든 문제가 끝났습니다!");
                    gameEndMessage.put("timestamp", System.currentTimeMillis());
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game/status", gameEndMessage);
                } else {
                    Map<String, Object> errorMessage = new HashMap<>();
                    errorMessage.put("message", "문제 정보를 찾을 수 없습니다.");
                    messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
                }
                return;
            }
            
            String answerListKey = String.format("quiz:%s:answers", quizId);
            Object answerObj = redisTemplate.opsForList().index(answerListKey, questionIndex);
            String correctAnswer = answerObj != null ? answerObj.toString().split(":")[1] : null;
            
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("questionIndex", questionIndex);
            questionData.put("questionText", questionObj.toString());
            questionData.put("correctAnswer", correctAnswer);
            questionData.put("timestamp", System.currentTimeMillis());
            
            String currentRoundKey = String.format("quiz:%s:currentRound", quizId);
            redisTemplate.opsForValue().set(currentRoundKey, String.valueOf(questionIndex), Duration.ofMinutes(30));
            
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/question", questionData);
            
        } catch (Exception e) {
            log.error("문제 정보 전송 중 오류 발생: {}", e.getMessage(), e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("message", "문제 정보 전송 중 오류가 발생했습니다: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/error", errorMessage);
        }
    }
}