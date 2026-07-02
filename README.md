# 공감퀴즈 💕

두 사람이 함께 10가지 질문에 답하고, 서로의 공감률을 확인하는 실시간 2인 퀴즈 게임입니다.

## 기능

- 방 생성 및 QR 코드 초대
- 실시간 진행 상태 동기화 (SSE)
- 10문항 랜덤 출제 (A/B 선택)
- 공감률 계산 및 결과 공유 (링크 복사, 카카오톡, 페이스북, 인스타그램, 쓰레드, X)

## 기술 스택

| 분류 | 기술 |
|------|------|
| 백엔드 | Spring Boot 4.1.0, Kotlin 2.3.21, Java 17 |
| 데이터베이스 | SQLite (WAL 모드), Spring Data JPA, Flyway |
| 템플릿 | Thymeleaf 3.1 |
| 프론트엔드 | Tailwind CSS (로컬), Font Awesome 6 (로컬), Noto Sans KR (로컬) |
| 실시간 통신 | SSE (Server-Sent Events) |

외부 CDN 의존성 없이 모든 정적 리소스를 로컬에서 서빙합니다.

## 실행 방법

### 사전 요구 사항

- Java 17+

### 로컬 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

기본 접속 주소: `http://localhost:8080`

`application-local.yml`에 context path가 설정된 경우: `http://localhost:8080/ogam`

## 설정

### application.yml

```yaml
app:
  db-path: ${DB_PATH:ogam.db}              # SQLite 파일 경로
  base-url: ${BASE_URL:http://localhost:8080}  # QR 코드 생성에 사용하는 서비스 외부 URL
```

### application-local.yml (예시)

```yaml
app:
  db-path: ./assets/ogam.db
server:
  servlet:
    context-path: /ogam
```

### 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `DB_PATH` | SQLite DB 파일 경로 | `ogam.db` |
| `BASE_URL` | 서비스 외부 접근 URL | `http://localhost:8080` |

## API

### 방 관리

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/rooms` | 새 방 생성 |
| `POST` | `/api/rooms/{code}/join` | 방 참가 |
| `GET` | `/api/rooms/{code}/status?playerId=` | 방 상태 조회 |
| `POST` | `/api/rooms/{code}/ready` | 준비 완료 |
| `POST` | `/api/rooms/{code}/answers` | 답변 제출 |
| `GET` | `/api/rooms/{code}/results?playerId=` | 결과 조회 |

### SSE

| 경로 | 설명 |
|------|------|
| `GET /sse/rooms/{code}` | 실시간 게임 이벤트 스트림 |

**SSE 이벤트 종류**

- `player_joined` — 상대방 입장
- `player_ready` — 준비 완료 수 변경
- `quiz_started` — 퀴즈 시작 (첫 문제 포함)
- `next_question` — 다음 문제
- `quiz_finished` — 퀴즈 종료

## 게임 플로우

```
[홈] → 새 방 만들기 → [대기 화면] QR 코드 / 방 코드 공유
                                         ↓ 상대방 입장
                       [준비 화면] 둘 다 시작하기 버튼 클릭
                                         ↓
                       [퀴즈 화면] 10문항 순서대로 A/B 선택 (선택 후 변경 불가)
                                         ↓ 둘 다 답변 완료 시 다음 문제
                       [결과 화면] 공감률 확인 + SNS 공유
```

## 프로젝트 구조

```
src/main/
├── kotlin/ia/antop/ogam/
│   ├── OgamApplication.kt
│   ├── config/
│   │   └── AppProperties.kt          # app.* 설정 바인딩
│   └── quiz/
│       ├── controller/
│       │   ├── PageController.kt     # 페이지 라우팅 (/, /room/{code})
│       │   ├── RoomApiController.kt  # REST API
│       │   └── SseController.kt      # SSE 스트림
│       ├── service/
│       │   ├── RoomService.kt
│       │   └── QuizService.kt
│       ├── entity/                   # JPA 엔티티
│       ├── repository/               # Spring Data 리포지토리
│       ├── dto/                      # 요청/응답 DTO
│       └── event/                    # Spring 이벤트 (SSE 트리거)
└── resources/
    ├── templates/
    │   ├── index.html                # 홈 화면
    │   └── room.html                 # 게임 화면
    ├── static/
    │   ├── js/                       # index.js, room.js, qrcode.min.js, tailwind.cdn.js
    │   ├── css/                      # room.css, index.css, fontawesome.min.css, noto-sans-kr.css
    │   ├── fonts/                    # Noto Sans KR woff2
    │   └── webfonts/                 # Font Awesome webfonts
    └── db/migration/
        ├── V1__ddl.sql               # 테이블 생성
        └── V2__dml.sql               # 기본 문항 20개 삽입
```
