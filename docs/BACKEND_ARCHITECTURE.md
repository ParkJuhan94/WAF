# WAF Console 백엔드 아키텍처

> 이 문서는 백엔드의 핵심 로직과 구조를 설명합니다. IDE에서 코드를 따라가며 분석할 때 참고하세요.

---

## 1. 패키지 구조

```
dev.waf.console/
├── BackendApplication.java          ← 메인 진입점

├── auth/                            ← 🔐 인증 도메인
│   ├── api/AuthController.java
│   ├── api/dto/GoogleLoginRequest.java
│   ├── api/dto/AuthResponse.java
│   └── service/AuthService.java

├── user/                            ← 👤 사용자 도메인
│   ├── domain/User.java
│   ├── domain/UserRole.java
│   └── repository/UserRepository.java

├── dashboard/                       ← 📊 대시보드 도메인
│   ├── api/DashboardController.java
│   ├── api/dto/WAFStatsResponse.java
│   ├── api/dto/WAFStatusResponse.java
│   ├── api/dto/TrafficDataResponse.java
│   ├── api/dto/AttackEventResponse.java
│   └── service/DashboardServiceImpl.java

├── waflog/                          ← 📝 로그 도메인
│   ├── domain/WAFLog.java
│   ├── api/WAFLogController.java
│   ├── service/WAFLogService.java
│   └── repository/WAFLogRepository.java

├── customrule/                      ← 🛡️ 커스텀 룰 도메인
│   ├── domain/CustomRule.java
│   ├── domain/RuleType.java
│   ├── domain/RuleSeverity.java
│   ├── api/CustomRuleController.java
│   ├── api/dto/CustomRuleRequest.java
│   ├── api/dto/CustomRuleResponse.java
│   ├── service/CustomRuleService.java
│   └── repository/CustomRuleRepository.java

├── config/                          ← ⚙️ 설정 클래스
│   ├── KafkaConfig.java             ★ 이벤트 스트리밍
│   ├── RedisConfig.java             ★ 캐시 설정
│   ├── ElasticsearchConfig.java     ★ 검색 엔진
│   ├── CacheConfig.java             ★ L1/L2 캐시
│   ├── AsyncConfig.java
│   └── WebSocketConfig.java

├── infrastructure/                  ← 🏗️ 인프라 계층
│   ├── config/SecurityConfig.java   ★ Spring Security + JWT
│   ├── security/JwtTokenProvider.java
│   ├── security/JwtAuthenticationFilter.java
│   └── logging/WAFLoggingInterceptor.java

├── service/                         ← 🔧 공유 서비스
│   ├── EventPublisher.java          ★ Kafka 발행
│   ├── EventConsumer.java           ★ Kafka 소비
│   ├── AlertServiceImpl.java        ★ 알림 서비스
│   └── WebSocketBroadcastService.java

├── event/                           ← 📨 이벤트 클래스
│   ├── WAFEvent.java (추상)
│   ├── AttackDetectedEvent.java
│   ├── AccessLogEvent.java
│   └── SecurityAlertEvent.java

└── common/                          ← 🔄 공통 유틸
    ├── dto/ApiResponse.java
    └── exception/GlobalExceptionHandler.java
```

---

## 2. API 엔드포인트 목록

### 🔐 인증 API (`/api/v1/auth`)

| Method | Endpoint | 설명 | 흐름 |
|--------|----------|------|------|
| POST | `/google` | Google OAuth 로그인 | AuthController → AuthService → UserRepository |
| POST | `/refresh` | JWT 토큰 갱신 | AuthController → AuthService → JwtTokenProvider |
| GET | `/me` | 현재 사용자 조회 | AuthController → AuthService → UserRepository |

### 📊 대시보드 API (`/api/dashboard`)

| Method | Endpoint | 설명 | 흐름 |
|--------|----------|------|------|
| GET | `/stats` | WAF 통계 | DashboardController → DashboardServiceImpl → WAFLogRepository |
| GET | `/status` | WAF 상태 | DashboardController → DashboardServiceImpl → CustomRuleService |
| GET | `/traffic?hours=24` | 트래픽 차트 | DashboardController → DashboardServiceImpl → WAFLogRepository |
| GET | `/attacks?limit=10` | 공격 이벤트 | DashboardController → DashboardServiceImpl → WAFLogService |

### 📝 로그 API (`/api/logs`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/recent` | 최근 로그 |
| GET | `/blocked` | 차단 로그 |
| GET | `/success` | 성공 로그 |
| GET | `/errors` | 에러 로그 |
| GET | `/high-risk` | 고위험 로그 (riskScore >= 70) |
| GET | `/statistics` | 로그 통계 |
| GET | `/range?startTime=&endTime=` | 기간별 조회 |
| GET | `/{id}` | 로그 상세 |
| GET | `/count` | 상태별 카운트 |

### 🛡️ 커스텀 룰 API (`/api/v1/rules`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 룰 목록 (페이징, 필터링) |
| GET | `/{id}` | 룰 상세 |
| GET | `/my` | 내가 만든 룰 |
| GET | `/active` | 활성 룰 (우선순위순) |
| GET | `/applicable?targetService=` | 특정 서비스 적용 가능 룰 |
| GET | `/statistics` | 룰 통계 |
| POST | `/` | 룰 생성 |
| PUT | `/{id}` | 룰 수정 |
| DELETE | `/{id}` | 룰 삭제 |
| PATCH | `/{id}/toggle` | 활성화/비활성화 |
| POST | `/{id}/match` | 매칭 기록 |
| POST | `/{id}/block` | 차단 기록 |

---

## 3. 핵심 비즈니스 로직 흐름

### 흐름 1: 대시보드 통계 조회

```
GET /api/dashboard/stats
         ↓
DashboardController.getWAFStats()
         ↓
DashboardServiceImpl.getWAFStats()
         ↓
WAFLogRepository.countByStatusSince()     ← 24시간 기준 상태별 카운트
WAFLogRepository.getAverageResponseTimeSince() ← 평균 응답시간
         ↓
WAFStatsResponse 반환
  - totalRequests: 총 요청 수
  - blockedRequests: 차단된 요청 수
  - allowedRequests: 허용된 요청 수
  - blockRate: 차단율 (%)
  - averageResponseTime: 평균 응답시간 (ms)
```

### 흐름 2: 트래픽 차트 데이터

```
GET /api/dashboard/traffic?hours=24
         ↓
DashboardController.getTrafficData()
         ↓
DashboardServiceImpl.getTrafficData()
         ↓
WAFLogRepository.getTrafficDataByHour()   ← DB 집계 쿼리 (GROUP BY)
         ↓
모든 시간대 생성 (없는 시간은 0으로 채움)
         ↓
List<TrafficDataResponse> 반환
  - timestamp: "yyyy-MM-dd HH:00:00"
  - totalRequests, blockedRequests, successRequests
  - avgResponseTime
```

### 흐름 3: WAF 로그 저장 (비동기)

```
HTTP 요청 → WAFLoggingInterceptor 가로채기
         ↓
WAFLogService.saveLogAsync()   ← @Async 비동기 처리
         ↓
WAFLogRepository.save()
         ↓
EventPublisher.publishAccessLog()  ← Kafka 발행
         ↓
Kafka Topic: waf.logs
```

### 흐름 4: 공격 탐지 이벤트 처리

```
Filebeat (Nginx error.log 수집)
         ↓
Kafka Topic: waf.attacks
         ↓
EventConsumer.handleAttackEvent()  ← @KafkaListener
         ↓
ModSecurityLogParser.parse()       ← 로그 파싱
         ↓
AlertServiceImpl.processAttack()   ← 알림 처리
         ↓
WebSocketBroadcastService.broadcast()  ← 대시보드 실시간 전송
```

### 흐름 5: Google OAuth 인증

```
POST /api/v1/auth/google
  Body: { "idToken": "..." }
         ↓
AuthController.googleLogin()
         ↓
AuthService.authenticateWithGoogle()
         ↓
Google ID Token 검증 (Google API Client)
         ↓
UserRepository.findByProviderAndProviderId()
  - 기존 사용자: 로그인
  - 신규 사용자: 자동 회원가입
         ↓
JwtTokenProvider.createAccessToken()  ← 1시간 유효
JwtTokenProvider.createRefreshToken() ← 30일 유효
         ↓
AuthResponse 반환
  - accessToken, refreshToken
  - expiresIn, tokenType: "Bearer"
  - userProfile: { id, email, name, role }
```

---

## 4. 인프라 설정 요약

### Docker Compose 서비스 구성

| 서비스 | 포트 | 역할 |
|--------|------|------|
| `waf` | 80 | ModSecurity + Nginx (리버스 프록시) |
| `web` | - | 보호 대상 백엔드 (테스트용) |
| `mysql` | 3306 | 데이터베이스 |
| `redis` | 6379 | 캐시 + 세션 |
| `zookeeper` | 2181 | Kafka 조율 |
| `kafka` | 9092 | 이벤트 스트리밍 |
| `elasticsearch` | 9200 | 로그 검색 |
| `filebeat` | - | Nginx 로그 수집 → Kafka 전송 |

### 주요 Config 클래스 역할

| 클래스 | 파일 위치 | 역할 |
|--------|----------|------|
| **SecurityConfig** | `infrastructure/config/` | JWT 필터 체인, CORS, 권한 설정 |
| **KafkaConfig** | `config/` | 5개 토픽 생성, Producer/Consumer 설정 |
| **RedisConfig** | `config/` | 7개 캐시 TTL 설정 |
| **CacheConfig** | `config/` | L1(Caffeine) + L2(Redis) 멀티레벨 캐시 |
| **ElasticsearchConfig** | `config/` | 5개 인덱스 템플릿 생성 |

### Kafka 토픽 구조

| 토픽 | 파티션 | 보존 기간 | 용도 |
|------|--------|----------|------|
| `waf.attacks` | 3 | 7일 | 공격 탐지 이벤트 |
| `waf.logs` | 3 | 1일 | 접근 로그 |
| `waf.alerts` | 3 | 30일 | 보안 알림 |
| `waf.metrics` | 3 | 3일 | 성능 메트릭 |
| `waf.audit` | 3 | 90일 | 감사 로그 |

### Redis 캐시 TTL

| 캐시명 | TTL | 용도 |
|--------|-----|------|
| `stats-cache` | 1분 | 실시간 통계 |
| `api-response` | 5분 | API 응답 |
| `log-search` | 3분 | 로그 검색 결과 |
| `rule-cache` | 10분 | WAF 규칙 |
| `whitelist-cache` | 15분 | IP 화이트리스트 |
| `user-profile` | 30분 | 사용자 정보 |
| `jwt-blacklist` | 1시간 | 토큰 블랙리스트 |

---

## 5. 핵심 엔티티

### WAFLog (`waflog/domain/WAFLog.java`)

```java
@Entity
public class WAFLog {
    Long id;
    LocalDateTime timestamp;        // 요청 시간 (updatable = false)

    // 요청 정보
    String sourceIp;
    String httpMethod;              // GET, POST, PUT, DELETE
    String requestUri;
    String userAgent;
    String sessionId;

    // 처리 결과
    WAFLogStatus status;            // SUCCESS, BLOCKED, ERROR, WARNING
    Integer responseStatusCode;     // 200, 403, 500...
    Long responseTimeMs;

    // 보안 분석
    String attackType;              // SQL_INJECTION, XSS, PATH_TRAVERSAL...
    Integer riskScore;              // 0-100
    String blockReason;
    String ruleId;
    String ruleName;

    String metadata;                // JSON 형태의 추가 정보
}
```

### User (`user/domain/User.java`)

```java
@Entity
public class User {
    Long id;
    String email;
    String name;
    String profileImage;

    UserRole role;                  // FREE_USER, PREMIUM_USER, ADMIN

    // OAuth 정보
    String provider;                // GOOGLE
    String providerId;

    LocalDateTime createdAt;
    LocalDateTime lastLoginAt;
}
```

### CustomRule (`customrule/domain/CustomRule.java`)

```java
@Entity
public class CustomRule {
    Long id;
    String name;
    String description;
    String ruleContent;             // ModSecurity 문법 룰

    RuleType type;                  // SQL_INJECTION, XSS, PATH_TRAVERSAL...
    RuleSeverity severity;          // LOW, MEDIUM, HIGH, CRITICAL

    Boolean enabled;                // 활성화 여부
    Integer priority;               // 우선순위 (높을수록 먼저 적용)
    String targetService;           // 적용 대상 서비스
    String targetPath;              // 적용 대상 경로

    // 통계
    Long matchCount;                // 매칭 횟수
    Long blockCount;                // 차단 횟수
    LocalDateTime lastMatchedAt;

    // 감사
    User createdBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

---

## 6. IDE에서 따라가기 좋은 순서

### 1단계: 진입점 파악

```
BackendApplication.java → 앱 시작
         ↓
SecurityConfig.java → 보안 필터 체인 (어떤 경로가 인증 필요한지)
         ↓
WebMvcConfig.java → 인터셉터 등록 (WAFLoggingInterceptor)
```

### 2단계: API 흐름 추적 (가장 많이 호출되는 API)

```
DashboardController
         ↓
DashboardServiceImpl
         ↓
WAFLogRepository (JPA 쿼리)
```

### 3단계: 이벤트 처리

```
EventPublisher (Kafka 발행)
         ↓
Kafka Topic
         ↓
EventConsumer (Kafka 소비)
         ↓
AlertServiceImpl (알림 처리)
```

### 4단계: 인증 흐름

```
JwtAuthenticationFilter (HTTP 필터)
         ↓
JwtTokenProvider (토큰 검증/생성)
         ↓
AuthService (비즈니스 로직)
```

---

## 7. 보안 설정

### Spring Security 권한 설정 (`SecurityConfig.java`)

```java
// 인증 불필요
- /api/v1/auth/**          // 로그인, 토큰 갱신
- /api/dashboard/**        // 대시보드 (현재 테스트용으로 열림)
- /actuator/health         // 헬스체크
- /ws/**                   // WebSocket

// 인증 필수
- 그 외 모든 경로
```

### JWT 토큰 구조

```
Access Token (1시간)
  - sub: 사용자 ID
  - email, name, role
  - iat, exp

Refresh Token (30일)
  - sub: 사용자 ID
  - iat, exp
```

---

## 8. 데이터 흐름 전체도

```
┌─────────────────────────────────────────────────────────────────┐
│                         외부 트래픽                               │
└─────────────────────────────┬───────────────────────────────────┘
                              ↓
                    ┌─────────────────────┐
                    │   WAF (Port 80)     │
                    │   ModSecurity CRS   │
                    │   ┌───────────────┐ │
                    │   │ 공격 탐지/차단  │ │
                    │   └───────┬───────┘ │
                    └───────────┼─────────┘
                                │
              ┌─────────────────┼─────────────────┐
              ↓                 ↓                 ↓
        error.log          access.log       웹 서버 (web)
              │                 │
              └────────┬────────┘
                       ↓
              ┌─────────────────┐
              │    Filebeat     │
              │   (로그 수집)    │
              └────────┬────────┘
                       ↓
              ┌─────────────────┐
              │     Kafka       │
              │  waf.attacks    │
              │  waf.logs       │
              └────────┬────────┘
                       ↓
              ┌─────────────────┐
              │  EventConsumer  │
              │   (Spring)      │
              └────────┬────────┘
                       │
         ┌─────────────┼─────────────┐
         ↓             ↓             ↓
   AlertService   Elasticsearch   MySQL
   (Slack 알림)    (로그 검색)   (정형 데이터)
         │
         ↓
   WebSocket Broadcast
         │
         ↓
┌─────────────────────────────────────────────────────────────────┐
│                     Dashboard (Port 3000)                        │
│                       실시간 모니터링                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 9. 자주 사용하는 Repository 쿼리

### WAFLogRepository

```java
// 상태별 카운트 (24시간 기준)
countByStatusSince(WAFLogStatus status, LocalDateTime since)

// 시간대별 트래픽 집계
getTrafficDataByHour(LocalDateTime start, LocalDateTime end)

// 평균 응답시간
getAverageResponseTimeSince(LocalDateTime since)

// 공격 유형별 통계
countBlockedByAttackTypeSince(LocalDateTime since)

// 상위 공격 IP
findTopAttackingIPs(int limit, LocalDateTime since)
```

### CustomRuleRepository

```java
// 활성화된 룰 (우선순위 순)
findByEnabledTrueOrderByPriorityDesc()

// 특정 서비스에 적용 가능한 룰
findApplicableRules(String targetService)

// 키워드 검색
findByKeyword(String keyword, Pageable pageable)

// 필터링 조회
findByFilters(Boolean enabled, RuleType type, RuleSeverity severity, String targetService, Pageable pageable)
```
