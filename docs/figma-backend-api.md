# Figma Backend Completion API

Twix Figma 화면에서 필요한 백엔드 보강 API를 정리한다. 모든 응답은 기존 `ApiResponse<T>` 형식을 사용한다.

## 인증 및 회원가입

### 아이디 사용 가능 여부

- `GET /api/auth/username-availability?username={username}`
- 영문, 숫자, `.`, `_`, `-` 조합의 4~20자만 사용할 수 있다.

```json
{
  "username": "twix_user",
  "available": true
}
```

### 회원가입 추가 필드

- `POST /api/auth/signup`
- 기존 필드에 아래 선택 필드가 추가됐다.

```json
{
  "username": "twix_user",
  "password": "password123",
  "passwordConfirm": "password123",
  "nickname": "트윅스",
  "ageGroup": "TWENTIES",
  "interests": ["anime", "game"],
  "gender": "FEMALE",
  "bio": "같이 덕질해요",
  "profileImageUrl": "/uploads/profile.png",
  "coverImageUrl": "/uploads/cover.png"
}
```

- `ageGroup`: `ALL`, `TEENS`, `TWENTIES`, `THIRTIES`, `FORTIES`, `FIFTIES`, `SIXTIES`, `SEVENTIES`
- `gender`: `MALE`, `FEMALE`, `UNSPECIFIED`
- 가입 성공 시 사용자와 기본 Profile이 같은 트랜잭션에서 생성된다.
- 관심사와 채팅방 해시태그는 앞의 `#`를 제거하고 소문자로 정규화해 저장한다.

## 파일 업로드

- `POST /api/uploads/images`: 이미지 업로드, multipart 필드명 `file`, 최대 10MB
- `POST /api/uploads/files`: 채팅 첨부 업로드, multipart 필드명 `file`, 최대 20MB
- 이미지 업로드는 회원가입 전 프로필/커버 이미지를 선택할 수 있도록 공개한다. 일반 파일 업로드는 JWT 인증이 필요하다.
- 업로드 후 반환된 `/uploads/{uuid}.{extension}` 값을 프로필 또는 메시지 요청에 사용한다.
- `GET`/`HEAD /uploads/**`는 이미지/첨부 표시와 사전 확인을 위해 인증 없이 접근할 수 있으며 모든 Origin에 공개한다.

```json
{
  "url": "/uploads/0f13c4c0-50c9-4cb2-a6be-cb5b31163c12.png",
  "originalFilename": "profile.png",
  "contentType": "image/png",
  "size": 12412
}
```

## 프로필 및 평점

- `GET /api/profile/me`
- `GET /api/profile/{userId}`
- `PUT /api/profile/me`

프로필 응답에는 `ageGroup`, `gender`, `interests`, `mannerScore`, `averageRating`, `ratingCount`가 포함된다. 참여 채팅방 항목에는 `summary`, `description`, `hashtags`, `ageConditions`, `gender`, `memberCount`가 포함된다.

프로필 수정은 기존 필드 외에 `nickname`, `ageGroup`, `gender`, `interests`를 받을 수 있다.

### 유저 평가

- `POST /api/users/{userId}/ratings`
- `GET /api/users/{userId}/ratings/summary`

```json
{
  "chatRoomId": 10,
  "score": 5
}
```

- 평가자와 대상자가 현재 같은 단체 채팅방 멤버여야 한다.
- 같은 평가자/대상/채팅방 조합은 새 행을 만들지 않고 기존 평점을 갱신한다.
- 별점은 1~5점이다. 매너점수는 기본 36.5에서 평균 별점 3점을 중립으로 두고 별 한 칸당 5점씩 반영한다.

팔로워/팔로잉 목록 항목에는 `interests`, `mannerScore`, `followerCount`, `followingCount`가 추가됐다.

## 단체 채팅

### 채팅방 탐색

- `GET /api/chat-rooms/{id}`: 채팅방 상세
- `GET /api/chat-rooms/recommended`: 로그인 유저의 관심사별 채팅방 그룹
- `GET /api/chat-rooms/me`: 참여 중인 채팅방 인박스
- `GET /api/chat-rooms/{id}/members`: 멤버 목록, 방 멤버만 조회 가능

`GET /api/chat-rooms/me` 응답에는 `lastMessage`, `lastMessageType`, `lastMessageAt`, `unreadCount`가 포함된다. 메시지 이력을 조회하면 해당 방의 최신 메시지까지 읽음 처리된다.

### 채팅방 생성

- `POST /api/chat-rooms`

```json
{
  "name": "애니 같이 봐요",
  "summary": "주말 정주행 멤버 모집",
  "description": "매주 토요일 저녁에 같이 봅니다.",
  "imageUrl": "/uploads/room.png",
  "hashtags": ["anime"],
  "ageConditions": ["TWENTIES", "THIRTIES"],
  "gender": "ANY",
  "extraConditions": ["매너 채팅"]
}
```

`POST /api/chat-rooms/{id}/join` 호출 시 아래 조건을 모두 검사한다.

- `ageConditions`가 비어 있지 않으면 유저 연령대가 포함돼야 한다.
- `gender`가 `ANY`가 아니면 유저 성별이 일치해야 한다.
- `hashtags`가 비어 있지 않으면 유저 관심사와 하나 이상 겹쳐야 한다.
- `extraConditions`는 자유 텍스트이므로 화면 표시용이며 자동 판정하지 않는다.

### 메시지

- `GET /api/chat-rooms/{id}/messages`
- `POST /api/chat-rooms/{id}/messages`

```json
{
  "content": "사진 올려요",
  "messageType": "IMAGE",
  "attachmentUrl": "/uploads/chat-image.webp"
}
```

- `messageType`: `TEXT`, `IMAGE`, `FILE`
- `TEXT`는 비어 있지 않은 `content`가 필요하다.
- `IMAGE`, `FILE`은 `attachmentUrl`이 필요하고 `content`는 선택이다.
- 메시지 응답에는 `senderNickname`, `senderProfileImageUrl`, `messageType`, `attachmentUrl`이 포함된다.

## 1:1 DM

- `POST /api/dm-rooms/{userId}`: 상대와 DM 방을 열거나 기존 방 반환
- `GET /api/dm-rooms`: 내 DM 인박스
- `GET /api/dm-rooms/{id}`: DM 방 정보
- `GET /api/dm-rooms/{id}/messages`: DM 메시지 이력 및 읽음 처리
- `POST /api/dm-rooms/{id}/messages`: DM 메시지 전송

같은 두 사용자 조합에는 하나의 방만 생성된다. 인박스에는 상대 프로필, 마지막 메시지/시각/유형, 안 읽은 수가 포함된다. 메시지 요청 형식은 단체 채팅과 같다.

## WebSocket

- Endpoint: `/ws`
- CONNECT 헤더: `Authorization: Bearer {accessToken}`
- 단체 채팅 전송/구독: `/app/chat-rooms/{roomId}`, `/topic/chat-rooms/{roomId}`
- DM 전송/구독: `/app/dm-rooms/{roomId}`, `/topic/dm-rooms/{roomId}`

CONNECT뿐 아니라 SEND와 SUBSCRIBE에서도 해당 방 멤버십을 검사한다. 클라이언트가 `/topic/**`으로 직접 SEND하는 요청은 거부된다.
