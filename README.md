# Group Location Sharing (GLSC)

그룹 단위로 실시간 위치를 공유하는 Spring Boot 백엔드 서비스입니다.

## 주요 구조 특징
- user - room 간 N:M 관계로 userroom 추가
- user - location 1:1 관계
- Room 번호(ex.ROOM-001), 휴대폰 번호 기반 조회, 수정
- Service기준의 input, output DTO 생성 및 관리
- Reactive 기반으로 개발 진행
- 사용자 참여 동시성 이슈 -> 쿼리단에서 해결

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Kotlin 2.1 |
| Framework | Spring Boot 3.4.5 |
| DB | H2 (In-Memory) |
| DB Access | Spring Data R2DBC + Coroutines |
| Async | Kotlin Coroutines |
| Runtime | Java 17 |

## 도메인 구조

```
users ──< user_rooms >── rooms
  │
  └──< locations
```

| 테이블 | 설명 |
|--------|------|
| `users` | 사용자 (이름, 전화번호) |
| `rooms` | 그룹 방 (방 번호, 방 이름, 생성자, 최대 인원) |
| `user_rooms` | 사용자-방 참여 관계 |
| `locations` | 사용자별 최신 위치 (user_id UNIQUE) |

방 번호는 시퀀스 기반으로 `ROOM-001` 형식으로 자동 발급됩니다.

## API

Base URL: `/api/v1`

### 그룹 생성

```
POST /rooms
```

**Request Body**
```json
{
  "roomName": "강남 달리기 모임",
  "creatorId": 1,
  "maxMembers": 5
}
```

**Response** `201 Created`
```json
{
  "id": 1,
  "roomNumber": "ROOM-001",
  "roomName": "강남 달리기 모임",
  "creatorId": 1,
  "maxMembers": 5,
  "createdAt": "2026-05-27T10:00:00"
}
```

> 생성자는 방 생성 시 자동으로 참여 처리됩니다.

---

### 소속된 그룹 목록 조회

```
GET /rooms?phoneNumber={전화번호}
```

**Response** `200 OK`
```json
[
  {
    "roomNumber": "ROOM-001",
    "roomName": "강남 달리기 모임",
    "creatorId": 1,
    "creatorName": "홍길동",
    "maxMembers": 5,
    "currentMembers": 3,
    "createdAt": "2026-05-27T10:00:00"
  }
]
```

---

### 그룹 참여

```
POST /rooms/{roomNumber}/join
```

**Request Body**
```json
{
  "phoneNumber": "010-2222-3333"
}
```

**Response** `200 OK`
```json
true
```

**예외 처리**
- 존재하지 않는 방 또는 사용자 → `400 Bad Request`
- 이미 참여한 사용자 → `500 Internal Server Error`
- 정원 초과 → `500 Internal Server Error`

> 참여 성공 시 기존 멤버 전원에게 SMS 알림이 발송됩니다.

---

### 그룹 위치 조회

```
GET /rooms/{roomNumber}/locations
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "latitude": 37.4979,
    "longitude": 127.0276,
    "recordedAt": "2026-05-27T10:00:00"
  }
]
```

> 해당 그룹에 속한 모든 멤버의 최신 위치를 반환합니다.

---

### 위치 업데이트

```
PUT /locations
```

**Request Body**
```json
{
  "userId": 1,
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

**Response** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "latitude": 37.5665,
  "longitude": 126.9780,
  "recordedAt": "2026-05-27T10:05:00"
}
```

> 어떤 방에도 참여하지 않은 사용자는 위치를 업데이트할 수 없습니다.

---

## SMS 알림

그룹 참여 이벤트 발생 시 외부 SMS 서버로 알림을 발송합니다.

- SMS 서버 주소는 `application.yaml`의 `sms.url`로 설정합니다. (기본값: `http://localhost:8090`)
- SMS 서버가 응답하지 않으면 로그 경고 후 예외를 발생시킵니다.

## 실행

```bash
./gradlew bootRun
```

애플리케이션 시작 시 `schema.sql`로 테이블을 생성하고, `data.sql`로 아래 초기 데이터를 삽입합니다.

| 사용자 | 전화번호 |
|--------|----------|
| 홍길동 | 010-1111-2222 |
| 김철수 | 010-2222-3333 |
| 이영희 | 010-3333-4444 |
| 박민준 | 010-4444-5555 |

| 방 번호 | 방 이름 | 참여 멤버 |
|---------|---------|-----------|
| ROOM-001 | 강남 달리기 모임 | 홍길동, 김철수, 이영희 |
| ROOM-002 | 한강 자전거 그룹 | 김철수, 이영희, 박민준 |
| ROOM-003 | 북한산 등산팀 | 홍길동, 박민준 |

## 설정

`src/main/resources/application.yaml`

```yaml
spring:
  r2dbc:
    url: r2dbc:h2:mem:///glscdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password:

sms:
  url: http://localhost:8090
```
