# 설계 문서: 인증 + User + 프로필 + 선호도

- 작성일: 2026-07-13
- 프로젝트: Fanz-Be (2026 해커톤 2조 — 덕질 커뮤니티/매칭 백엔드)
- 범위(첫 서브프로젝트): 회원가입, 로그인(+리프레시), 프로필 설정, 유저 선호도(매너온도)
- 스택: Spring Boot 4.1.0, Kotlin, JPA(MySQL), Spring Security, JWT

---

## 1. 목표와 범위

기능 명세서의 4개 기능을 첫 서브프로젝트로 묶어 구현한다. 나머지 3개 서브시스템(덕메 구인 게시판, 채팅)은 이후 별도 spec으로 진행하며, 모두 여기서 만드는 `User`에 의존한다.

- 회원가입 (`/api/auth/signup`)
- 로그인 + 토큰 재발급 + 로그아웃 (`/api/auth/*`)
- 프로필 설정 (`/api/profile/me`)
- 유저 선호도 = **매너온도형 평판 점수** 조회/증감

**비범위(YAGNI):** 별도 계정 인증, 소셜 로그인, 비밀번호 재설정, 이미지 업로드(파일 스토리지)는 이번 범위에서 제외. 프로필 이미지는 URL 문자열만 저장.

---

## 2. 아키텍처

평범한 **계층 구조**: `Controller → Service → Repository`. 도메인별 패키지 분리.

```
com.example.fanzbe
 ├─ global
 │   ├─ config        SecurityConfig, WebConfig
 │   ├─ security      JwtProvider, JwtAuthenticationFilter, CustomUserDetails(Service)
 │   ├─ common        BaseTimeEntity, ApiResponse
 │   └─ exception     GlobalExceptionHandler, BusinessException, ErrorCode
 └─ domain
     ├─ user          UserController/Service/Repository, User(entity), dto
     │   └─ auth       AuthController/Service, RefreshToken(entity)/Repository, dto
     ├─ profile       ProfileController/Service/Repository, Profile(entity), dto
     └─ preference    PreferenceController/Service, (점수는 User에 저장), dto
```

---

## 3. 도메인 모델

### User (`users`)
| 필드 | 타입 | 비고 |
|---|---|---|
| id | Long PK | auto |
| password | String, not null | BCrypt 해시 |
| nickname | String, unique, not null | 로그인 ID 및 유저 검색 기준 |
| hashtags | ElementCollection\<String\> | 유저 관심 해시태그 |
| mannerScore | Double, not null | **선호도/매너온도**. 기본값 36.5 |
| role | Enum(USER, ADMIN) | 기본 USER |
| createdAt / updatedAt | (BaseTimeEntity) | |

- **선호도 점수는 User에 필드로 저장**(당근 매너온도처럼 유저 1개당 하나). 별도 카테고리 없음.

### Profile (`profiles`) — User와 1:1
| 필드 | 타입 | 비고 |
|---|---|---|
| id | Long PK | |
| user | OneToOne(User), unique FK | |
| bio | String(길이 제한, nullable) | 자기소개 |
| profileImageUrl | String, nullable | URL만 저장 |
| interests | ElementCollection<String> | 관심사 태그(단순 문자열 집합) |

- 회원가입 시 빈 Profile을 함께 생성(모든 User는 Profile 1개 보유)할지, 최초 수정 시 생성할지 → **회원가입 시 함께 생성**으로 통일(조회 편의).

### RefreshToken (`refresh_tokens`)
| 필드 | 타입 | 비고 |
|---|---|---|
| id | Long PK | |
| userId | Long, unique | 유저당 1개(재발급 시 회전) |
| token | String | 리프레시 토큰 문자열 |
| expiresAt | LocalDateTime | 만료 |

- Redis 미도입이므로 **DB 테이블에 저장**. 재발급 시 rotate(기존 삭제 후 신규 저장), 로그아웃 시 삭제.

---

## 4. 인증 설계 (JWT + Refresh)

- **Access Token**: 짧은 만료(예: 30분). Authorization: Bearer 헤더로 전달. Stateless.
- **Refresh Token**: 긴 만료(예: 14일). DB 저장. Access 만료 시 재발급에 사용.
- Security 필터(`JwtAuthenticationFilter`)가 요청마다 Access 검증 → `SecurityContext`에 인증 주입.
- 비밀번호는 `BCryptPasswordEncoder`로 해시.
- 시크릿/만료값은 `application.yaml`의 `jwt.*` 설정에서 주입.

### 토큰 흐름
1. 로그인 성공 → Access + Refresh 발급, Refresh는 DB 저장.
2. Access 만료 → `POST /api/auth/reissue`에 Refresh 전달 → 검증(존재+미만료) → **새 Access + 새 Refresh 발급, 기존 Refresh 회전(rotation)**.
3. 로그아웃 → 해당 유저 Refresh 삭제.

---

## 5. API 명세

응답은 공통 래퍼 `ApiResponse<T>` 사용(성공/에러 통일). 인증 필요 엔드포인트는 (🔒).

### 인증 (feature/signup, feature/login)
| 기능 | 메서드 | 경로 | 요청 | 응답 |
|---|---|---|---|---|
| 회원가입 | POST | `/api/auth/signup` | nickname, password, hashtags | 생성된 userId |
| 로그인 | POST | `/api/auth/login` | nickname, password | accessToken, refreshToken |
| 토큰 재발급 | POST | `/api/auth/reissue` | refreshToken | accessToken, refreshToken |
| 로그아웃 🔒 | POST | `/api/auth/logout` | (인증 유저) | 204 |

### 프로필 (feature/profile)
| 기능 | 메서드 | 경로 | 요청 | 응답 |
|---|---|---|---|---|
| 내 프로필 조회 🔒 | GET | `/api/profile/me` | - | bio, imageUrl, interests, nickname |
| 내 프로필 수정 🔒 | PUT | `/api/profile/me` | bio, profileImageUrl, interests | 수정된 프로필 |

### 선호도(매너온도) (feature/preference)
| 기능 | 메서드 | 경로 | 요청 | 응답 |
|---|---|---|---|---|
| 유저 선호도 조회 | GET | `/api/users/{userId}/preference` | - | mannerScore |
| 선호도 증감 🔒 | PATCH | `/api/users/{userId}/preference` | delta(+/- 값) | 갱신된 mannerScore |

- **가정:** "선호도를 늘리거나 줄일 수 있는" = 다른 유저가 대상 유저를 평가해 매너온도를 올리거나 내림. 증감 폭/평가 중복 방지 정책은 초기엔 단순화(요청 delta를 서버에서 허용 범위로 clamp). 자기 자신 평가 불가.
- 점수 하한/상한(예: 0.0 ~ 99.9)과 1회 증감 폭 제한은 Service에서 검증.

---

## 6. 검증 / 예외 처리

- 요청 DTO는 `jakarta.validation`(@NotBlank, @Size 등)으로 검증.
- `GlobalExceptionHandler`(@RestControllerAdvice)에서 예외 → `ApiResponse` 에러 포맷으로 변환.
- 주요 에러: 닉네임 중복(회원가입), 인증 실패(로그인), 토큰 무효/만료(재발급), 리소스 없음(유저/프로필), 자기 자신 평가(선호도).

---

## 7. 테스트 전략

- 단위 테스트: Service 로직(비밀번호 해시, 토큰 검증, 점수 clamp).
- 슬라이스/통합 테스트: 인증 플로우(회원가입→로그인→보호된 엔드포인트 접근), 프로필 수정, 선호도 증감. 테스트 DB는 H2(인메모리).

---

## 8. 브랜치 / PR 계획

| PR | 브랜치 | 내용 |
|---|---|---|
| #1 | `chore/base-setup` | 패키지 구조, `global/*`(config·security·common·exception), `User` 엔티티 + Repository, SecurityConfig, JwtProvider/Filter 기반 |
| #2 | `feature/signup` | 회원가입 |
| #3 | `feature/login` | 로그인 + 재발급 + 로그아웃 (RefreshToken 포함) |
| #4 | `feature/profile` | 프로필 조회/수정 |
| #5 | `feature/preference` | 선호도(매너온도) 조회/증감 |

각 PR은 base 위에서 분기. 구현은 **Codex에게 위임**, **Claude가 리뷰·수정** 후 PR.

---

## 9. 확정 필요/가정 정리

- (가정) 선호도 = 유저 단일 매너온도, 기본 36.5, 다른 유저가 증감. — 사용자 확인 완료.
- (가정) Refresh Token은 DB 저장, 유저당 1개, 재발급 시 회전.
- (미확정) 매너온도 하한/상한/증감 폭 구체 수치 — 구현 시 상수로 두고 조정 가능.
