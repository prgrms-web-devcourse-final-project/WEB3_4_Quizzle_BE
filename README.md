# 🧠 Quizzle - AI 기반 실시간 온라인 퀴즈 플랫폼

> **AI 자동 퀴즈 생성 + 실시간 멀티플레이**가 결합된 인터랙티브 학습 게임 플랫폼

---

## 🏁 프로젝트 개요

- **Quizzle**은 GPT API를 활용한 **자동 퀴즈 생성**과  
  WebSocket + Redis Pub/Sub을 통한 **실시간 멀티플레이** 게임 환경을 제공합니다.
- 사용자 간 **정답 경쟁**, **포인트 보상**, **아바타 커스터마이징**을 통해  
  학습과 재미를 동시에 잡는 플랫폼입니다.

---

## 🔧 기술 스택

| 분류 | 기술 |
|------|------|
| Frontend | Next.js, Vercel |
| Backend | Spring Boot 3, Java 21, Gradle |
| AI 연동 | OpenAI GPT API |
| Auth | OAuth2, JWT, Kakao/Google Login |
| 실시간 통신 | WebSocket (STOMP), Redis Pub/Sub |
| 인프라 | Docker, AWS EC2, S3, GitHub Actions |
| DB | MySQL (prod), H2 (dev/test) |

---

## 🔑 주요 기능

- ✅ **소셜 로그인** (Google, Kakao)
- ✅ **GPT 기반 퀴즈 생성** (카테고리, 난이도 선택)
- ✅ **실시간 게임 진행** (WebSocket + Redis)
- ✅ **포인트 시스템 및 아바타 구매**
- ✅ **친구 추가**
- ✅ **게임 결과 리포트**
- ✅ **경험치/레벨 시스템**

--

## 📌 ERD
![ERD](https://github.com/user-attachments/assets/a6c4d070-3617-4332-8912-5c680f767c7a)


## 📌 시스템 아키텍처
![아키텍처 다이어그램](https://github.com/user-attachments/assets/c81cb60c-c027-4db7-8834-5ce44fffc331)

---
## 💾 발표 자료
- [결과보고서_10팀(AI 퀴즈 게임 플랫폼 Quizzle).pdf](https://github.com/user-attachments/files/19770113/_10.AI.Quizzle.pdf)
- https://youtu.be/VlVQMjY-zoY
---

## 🧪 시연 영상

- https://youtu.be/PAbJh_QYOyo

---

## 👥 팀 소개

| 역할 | 이름 |
|------|------|
| PO | 권기용 |
| 팀장 | 이상억 |
| 팀원 | 노옥선, 박영준, 백성현 |

---

## 🥇FrontEnd Repository
- https://github.com/jurio5/secret_fe

---
## 📝 회고 및 느낀 점

- 실시간 통신, OAuth2, GPT API, Docker, CI/CD 등  
  **실무 중심의 기술 스택을 통합 경험**
- 분산 락과 트랜잭션 관리, 성능 안정화 등  
  **대규모 트래픽을 고려한 설계 적용**
- PR 기반 협업 및 코드 리뷰를 통해  
  **팀워크와 협업 역량 향상**
