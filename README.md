# WEB3_4_Quizzle_BE

## Websocket 설정 및 message
```
### 필수 요구사항

- Java 17 이상

- Redis

- H2
### Redis 설치

#### Windows

1. [Redis for Windows](https://github.com/microsoftarchive/redis/releases) 에서 최신 버전 다운로드

2. Redis 서버 실행:

```bash

redis-server

```
  
#### Mac

```bash

brew install redis

brew services start redis

```
#### Linux (Ubuntu)

```bash

sudo apt-get update

sudo apt-get install redis-server

sudo systemctl start redis

```

### 프로젝트 설정

1. 프로젝트 클론

```bash

git clone https://github.com/prgrms-web-devcourse-final-project/WEB3_4_Quizzle_BE.git

```

2. 환경 설정 파일 추가

- 프로젝트 루트 디렉토리에 `.env` 파일 생성 (슬랙 PR 채널 탭)

- `src/main/resources/` 디렉토리에 `application-secret.yml` 파일 추가 (슬랙 PR 채널 탭)

2. 애플리케이션 실행
##  WebSocket API

### 연결 정보

- 엔드포인트: `/ws`

- JWT 토큰은 쿠키에 포함하여 전송
### 메시지 전송 엔드포인트

#### 로비

- `/app/lobby`: 로비 상태 메시지 전송

- `/app/lobby/chat`: 로비 채팅 메시지 전송
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

  roomId?: string  // 방/게임 채팅에만 필요

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
### 오류 부분은 여기서 체크 해주세요!

1. Redis 연결 오류
- Redis 서버가 실행 중인지 확인
- Redis 기본 포트(6379)가 사용 가능한지 확인

2. JWT 토큰 관련 오류
- 쿠키에 access_token이 제대로 포함되어 있는지 확인
- 토큰이 만료되지 않았는지 확인
