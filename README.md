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

  data?: string,  // id,name,isReady,isOwner 포함
  {
    "id": "1",
    "name": "홍길동",
    "isReady": false,
    "isOwner": true
  },
  {
    "id": "2",
    "name": "김철수",
    "isReady": true,
    "isOwner": false
  },
  ...

  timestamp: number,

  roomId: string
}
```
#### ActiveUsersDTO
```typescript
[
  {
    email: string,       // 사용자 이메일
    sessions: string[],  // 세션 ID 목록
    lastActive: number,  // 마지막 활동 시간 (timestamp)
	// 상태 (현재는 항상 'online', 세션 종료 시 오프라인 표시 필요X)
	// 이 부분은 로비에서 실시간 접속 중인 플레이어 목록을 확인하는 부분
    status: string       
  },
]
```

### 이벤트 발생 시점 (후크 메서드로 관리)

- `/topic/lobby/users`: 사용자가 연결될 때, 연결이 끊길 때, 명시적으로 요청할 때 발생

### 오류 부분은 여기서 체크 해주세요!

1. Redis 연결 오류

- Redis 서버가 실행 중인지 확인

- Redis 기본 포트(6379)가 사용 가능한지 확인

2. JWT 토큰 관련 오류

- 쿠키에 access_token이 제대로 포함되어 있는지 확인

- 토큰이 만료되지 않았는지 확인