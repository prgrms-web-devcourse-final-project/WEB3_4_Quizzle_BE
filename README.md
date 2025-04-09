# WEB3_4_Quizzle_BE

## WebSocket API

### 연결 정보

- 엔드포인트: `/ws`

- JWT 토큰은 쿠키에 포함하여 전송
### 메시지 전송 엔드포인트

#### 로비

- `/app/lobby`: 로비 상태 메시지 전송

- `/app/lobby/chat`: 로비 채팅 메시지 전송

- `/app/lobby/users`: 로비 접속자 목록 요청

#### 방

- `/app/room/{roomId}`: 방 상태 메시지 전송

- `/app/room/chat/{roomId}`: 방 채팅 메시지 전송

#### 게임

- `/app/game/{roomId}`: 게임 상태 메시지 전송

- `/app/game/start/{roomId}`: 게임 시작 메시지 전송

- `/app/game/chat/{roomId}`: 게임 채팅 메시지 전송

#### 퀴즈
- `/app/quiz/{quizId}/submit` : 퀴즈 답안 제출 메시지 전송

### 구독 주제

#### 로비

- `/topic/lobby`: 로비 상태 업데이트 수신

- `/topic/lobby/chat`: 로비 채팅 메시지 수신

- `/topic/lobby/users`: 로비 접속자 목록 수신

#### 방

- `/topic/room/{roomId}`: 방 상태 업데이트 수신

- `/topic/room/chat/{roomId}`: 방 채팅 메시지 수신

#### 게임

- `/topic/game/{roomId}`: 게임 상태 업데이트 수신

- `/topic/game/start/{roomId}`: 게임 시작 이벤트 수신

- `/topic/game/chat/{roomId}`: 게임 채팅 메시지 수신

#### 퀴즈

- `/topic/quiz/{quizId}/updates`: 퀴즈 제출 결과 및 상태 업데이트 수신
### 메시지 형식

#### ChatMessageDTO
```typescript

{
  type: 'CHAT' | 'JOIN' | 'LEAVE' | 'SYSTEM' | 'WHISPER',

  content: string,

  senderId: string,

  senderName: string,

  timestamp: number,

  roomId?: string  // 방/게임 채팅에만 필요

}
```

#### RoomMessageDTO
```typescript

{

type: 'JOIN' | 'LEAVE' | 'READY' | 'UNREADY' | 'GAME_START' | 'GAME_END'

| 'ANSWER_SUBMIT' | 'TIMER' | 'ROUND_START' | 'ROUND_END' | 'SYSTEM',

senderId: string,

senderName: string,

content?: string,

data?: string,  // JSON 형식의 추가 데이터 (예: 참가자 목록, 준비 상태 등)

timestamp: number,

roomId: string

}
```
#### WebSocketQuizSubmitResponse
```
{
  type: 'ANSWER_SUBMIT',
  questionNumber: number,
  correct: boolean,
  correctAnswer: string,
  memberId: string,
  nickname: string,
  isSubmitted: boolean,
  timestamp: number,
  quizId: string
}
```
