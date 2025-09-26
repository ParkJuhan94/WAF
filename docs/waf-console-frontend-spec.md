# WAF SaaS 플랫폼 통합 명세서

## 1. 프로젝트 개요

### 목표
ModSecurity + OWASP CRS 기반 **WAF SaaS 플랫폼** 개발
- **멀티테넌트**: Google OAuth 기반 사용자별 대시보드
- **환불 조건 대응**: DVWA 정상 요청 통과, 5가지 공격 유형 차단 증명
- **실시간 트래픽 모니터링**: 차트 기반 실시간 데이터 시각화

### 핵심 요구사항
- **SaaS 아키텍처**: 다중 사용자 지원, 사용자별 데이터 격리
- **Google 소셜 로그인**: OAuth 2.0 기반 인증 시스템
- **실시간 대시보드**: WebSocket 기반 실시간 로그/통계 스트리밍
- **커스텀 룰 관리**: 웹 기반 ModSecurity 룰 편집기
- **다크 테마**: 보안 운영 환경에 최적화된 UI/UX

## 2. 기술 스택

### Frontend
- **React 18** + **Vite** (빠른 개발 서버, HMR)
- **TypeScript** (타입 안정성)
- **Ant Design** (다크 테마 지원, 관리자 콘솔 최적화)

### 상태 관리
- **Zustand** (전역 상태 관리)
- **React Query** (서버 상태 관리, 실시간 데이터 캐싱)

### 차트 & 시각화
- **Apache ECharts** (실시간 차트, 다크 테마 지원)
- **React-ECharts** (React 바인딩)

### Backend (기존 설계)
- **Spring Boot** + **JPA** (REST API)
- **JWT 인증** (Google OAuth 연동)
- **사용자별 데이터 격리**

### 배포
- **Frontend**: Vercel (무료, Git 연동 자동 배포, CDN)
- **Backend**: Docker 컨테이너 기반
- **환경변수**: 플랫폼별 환경변수로 설정 관리

## 3. SaaS 아키텍처 설계

### 멀티테넌트 구조
```javascript
사용자 격리 전략:
📊 데이터베이스: 사용자별 스키마 분리
🔐 인증: Google OAuth + JWT 토큰
📝 로그: 사용자 ID 기반 필터링
⚙️ 룰: 사용자별 커스텀 룰 네임스페이스
🌐 도메인: 사용자별 서브도메인 지원 (선택사항)
```

### 권한 매트릭스
| 페이지 | 미인증 | 일반 사용자 | 프리미엄 사용자 | 관리자 |
|--------|--------|-------------|----------------|--------|
| /login | ✅ | ❌ | ❌ | ❌ |
| /dashboard | ❌ | ✅ | ✅ | ✅ |
| /rules | ❌ | ✅ (읽기) | ✅ (쓰기) | ✅ (전체) |
| /analytics | ❌ | ✅ (기본) | ✅ (고급) | ✅ (전체) |
| /settings | ❌ | ✅ (개인) | ✅ (개인+알림) | ✅ (시스템) |
| /profile | ❌ | ✅ | ✅ | ✅ |
| /admin | ❌ | ❌ | ❌ | ✅ |

## 4. 라우팅 구조 및 페이지 설계

### React Router 설정
```typescript
<Routes>
  <Route path="/login" element={<LoginPage />} />
  <Route path="/" element={<ProtectedRoute />}>
    <Route index element={<Navigate to="/dashboard" />} />
    <Route path="dashboard" element={<DashboardPage />} />
    <Route path="rules" element={<RulesPage />} />
    <Route path="rules/:id/edit" element={<RuleEditorPage />} />
    <Route path="analytics" element={<AnalyticsPage />} />
    <Route path="analytics/reports" element={<ReportsPage />} />
    <Route path="settings" element={<SettingsPage />} />
    <Route path="settings/alerts" element={<AlertSettingsPage />} />
    <Route path="profile" element={<ProfilePage />} />
    <Route path="admin" element={<AdminPage />} />
  </Route>
  <Route path="*" element={<NotFound />} />
</Routes>
```

### URL 구조
```
https://waf-saas.com/
├── /login                 # Google OAuth 로그인
├── /dashboard             # 메인 대시보드 (실시간 모니터링)
├── /rules                 # 룰 관리 목록
├── /rules/:id/edit        # 룰 편집기
├── /rules/create          # 새 룰 생성
├── /analytics             # 분석 대시보드
├── /analytics/reports     # 상세 보고서
├── /settings              # 시스템 설정
├── /settings/alerts       # 알림 설정
├── /profile               # 사용자 프로필
└── /admin                 # 관리자 패널
```

## 5. 프로젝트 구조 (확장)

```
waf-saas-console/
├── src/
│   ├── components/
│   │   ├── auth/
│   │   │   ├── GoogleLogin.tsx         # 구글 OAuth 로그인
│   │   │   ├── ProtectedRoute.tsx      # 인증 보호 라우트
│   │   │   └── UserProfile.tsx         # 사용자 프로필 드롭다운
│   │   ├── layout/
│   │   │   ├── AppLayout.tsx           # 메인 레이아웃
│   │   │   ├── Header.tsx              # 헤더 (로그인 상태, 알림)
│   │   │   ├── Sidebar.tsx             # 사이드바 네비게이션
│   │   │   └── Footer.tsx              # 푸터
│   │   ├── dashboard/
│   │   │   ├── TrafficChart.tsx        # 실시간 트래픽 차트
│   │   │   ├── StatsCards.tsx          # 통계 카드 그리드
│   │   │   ├── RecentAttacks.tsx       # 최근 공격 피드
│   │   │   ├── SystemStatus.tsx        # WAF 시스템 상태
│   │   │   ├── QuickActions.tsx        # 빠른 액션 버튼
│   │   │   ├── LogStream.tsx           # 실시간 로그 스트림
│   │   │   └── AttackHeatmap.tsx       # 공격 지도 시각화
│   │   ├── rules/
│   │   │   ├── RuleManager.tsx         # 룰 관리 패널
│   │   │   ├── RuleList.tsx            # 룰 목록 테이블
│   │   │   ├── RuleEditor.tsx          # ModSecurity 룰 에디터
│   │   │   ├── RuleTester.tsx          # 룰 테스트 도구
│   │   │   ├── RuleBackup.tsx          # 룰 백업/복원
│   │   │   └── RuleHistory.tsx         # 룰 변경 이력
│   │   ├── analytics/
│   │   │   ├── AttackTypeChart.tsx     # 공격 유형별 통계
│   │   │   ├── TrendAnalysis.tsx       # 시간대별 트렌드
│   │   │   ├── ThreatIntel.tsx         # 위협 인텔리전스
│   │   │   ├── ReportGenerator.tsx     # 보고서 생성기
│   │   │   └── AlertSettings.tsx       # 알림 규칙 설정
│   │   ├── logs/
│   │   │   ├── LogViewer.tsx           # 로그 뷰어 (검색, 필터)
│   │   │   ├── LogDetail.tsx           # 로그 상세 모달
│   │   │   ├── LogExporter.tsx         # 로그 내보내기
│   │   │   └── AttackEvidence.tsx      # 차단 증거 수집
│   │   ├── settings/
│   │   │   ├── SystemSettings.tsx      # 시스템 설정
│   │   │   ├── NotificationSettings.tsx # 알림 설정
│   │   │   ├── APIKeyManager.tsx       # API 키 관리
│   │   │   ├── UserManagement.tsx      # 사용자 관리
│   │   │   └── AuditLog.tsx            # 감사 로그
│   │   ├── testing/
│   │   │   ├── AttackSimulator.tsx     # 공격 시뮬레이터
│   │   │   ├── DVWAIntegration.tsx     # DVWA 테스트 연동
│   │   │   ├── TestReport.tsx          # 환불 조건 검증 리포트
│   │   │   └── PenetrationTest.tsx     # 침투 테스트 도구
│   │   └── common/
│   │       ├── Loading.tsx             # 로딩 스피너
│   │       ├── ErrorBoundary.tsx       # 에러 경계
│   │       ├── StatusBadge.tsx         # 상태 표시 배지
│   │       ├── Chart.tsx               # 차트 공통 컴포넌트
│   │       └── Modal.tsx               # 모달 래퍼
│   ├── pages/
│   │   ├── LoginPage.tsx               # 로그인 페이지
│   │   ├── DashboardPage.tsx           # 메인 대시보드
│   │   ├── RulesPage.tsx               # 룰 관리 페이지
│   │   ├── RuleEditorPage.tsx          # 룰 편집 페이지
│   │   ├── AnalyticsPage.tsx           # 분석 페이지
│   │   ├── ReportsPage.tsx             # 보고서 페이지
│   │   ├── SettingsPage.tsx            # 설정 페이지
│   │   ├── AlertSettingsPage.tsx       # 알림 설정
│   │   ├── ProfilePage.tsx             # 사용자 프로필
│   │   ├── AdminPage.tsx               # 관리자 패널
│   │   └── NotFoundPage.tsx            # 404 페이지
│   ├── stores/
│   │   ├── useAuthStore.ts             # 인증 상태 (Google OAuth)
│   │   ├── useWAFStore.ts              # WAF 상태
│   │   ├── useLogStore.ts              # 로그 필터 상태
│   │   ├── useRuleStore.ts             # 룰 관리 상태
│   │   └── useNotificationStore.ts     # 알림 상태
│   ├── hooks/
│   │   ├── useRealtimeData.ts          # 실시간 데이터 훅
│   │   ├── useAttackSimulator.ts       # 공격 시뮬레이션 훅
│   │   ├── useLogExporter.ts           # 로그 내보내기 훅
│   │   ├── useWebSocket.ts             # WebSocket 연결 훅
│   │   └── useAuth.ts                  # 인증 관련 훅
│   ├── services/
│   │   ├── api.ts                      # REST API 클라이언트
│   │   ├── auth.ts                     # Google OAuth 서비스
│   │   ├── websocket.ts                # WebSocket 연결
│   │   ├── attackTests.ts              # 공격 테스트 시나리오
│   │   └── exportService.ts            # 데이터 내보내기
│   ├── types/
│   │   ├── api.ts                      # API 타입 정의
│   │   ├── auth.ts                     # 인증 관련 타입
│   │   ├── waf.ts                      # WAF 관련 타입
│   │   ├── logs.ts                     # 로그 관련 타입
│   │   └── user.ts                     # 사용자 관련 타입
│   ├── utils/
│   │   ├── formatters.ts               # 데이터 포매팅
│   │   ├── chartConfig.ts              # 차트 설정
│   │   ├── constants.ts                # 상수 정의
│   │   ├── permissions.ts              # 권한 체크 유틸
│   │   └── validators.ts               # 입력 검증
│   ├── App.tsx                         # 메인 앱 컴포넌트
│   └── main.tsx                        # 엔트리 포인트
├── public/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js                  # Ant Design 커스터마이징
└── vercel.json                         # Vercel 배포 설정
```

## 6. 페이지별 상세 기능

### 6.1 로그인 페이지 (`/login`)
```typescript
// Google OAuth 2.0 연동
- "Google로 로그인" 버튼
- OAuth 리다이렉트 처리
- JWT 토큰 저장 및 인증 상태 관리
- 로그인 성공 시 대시보드로 자동 이동
```

### 6.2 메인 대시보드 (`/dashboard`)
**실시간 모니터링**
- 🔄 실시간 트래픽 차트 (WebSocket)
- 📊 공격 유형별 통계 (OWASP Top 10 기준)
- 🌍 지리적 공격 분포 히트맵
- ⚡ 응답 시간 및 처리량 모니터링
- 🚨 실시간 위험 알림

**기존 기능 (개선)**
- HTTP 메서드별 필터링 → **사용자별 필터링 추가**
- 상태 코드별 필터링 → **공격 심각도별 필터링**
- WAF 룰별 필터링 → **커스텀 룰 포함**

### 6.3 룰 관리 (`/rules`)
```typescript
핵심 기능:
✅ OWASP CRS 룰 관리 (활성화/비활성화)
✅ 커스텀 룰 CRUD (ModSecurity 문법)
🆕 룰 백업/복원 시스템
🆕 룰 테스트 시뮬레이션
🆕 룰 적용 이력 관리
🆕 룰 성능 분석 및 최적화
🆕 사용자별 룰 네임스페이스
```

### 6.4 분석 페이지 (`/analytics`)
```typescript
고급 분석 기능:
📈 공격 패턴 트렌드 분석
📊 사용자별 공격 현황 비교
🎯 차단률/오탐률 분석
🔍 이상 패턴 AI 감지
📋 주간/월간 보안 리포트
💾 맞춤형 대시보드 구성
```

### 6.5 설정 페이지 (`/settings`)
```typescript
시스템 관리:
⚙️ WAF 엔진 파라미터 조정
🔔 다중 채널 알림 (이메일, Slack, Discord, Teams)
🔑 개인/팀 API 키 관리
🏥 멀티 노드 헬스 모니터링
📝 상세 감사 로그
🗂️ 데이터 보관 정책 설정
```

### 6.6 사용자 프로필 (`/profile`)
```typescript
개인 설정:
👤 Google 계정 연동 정보
🔐 개인 API 키 발급/관리
📧 개인화된 알림 설정
🌙 UI 테마 및 언어 설정
📊 개인 사용량 통계
💳 구독 플랜 관리 (SaaS)
```

## 7. 디자인 시스템 (기존 + 확장)

### 컬러 팔레트 (다크 테마)
```css
/* 메인 컬러 (기존) */
--bg-primary: #1b2431      /* 메인 배경 */
--accent-primary: #1ec997   /* 포인트 컬러 (성공, 활성화) */

/* 상태 컬러 (기존) */
--danger: #ff6b6b          /* 위험, 차단 */
--warning: #feca57         /* 경고, 탐지 */
--info: #48cae4            /* 정보 */
--success: #1ec997         /* 성공, 정상 */

/* 사용자 등급별 컬러 (추가) */
--tier-free: #6c757d       /* 무료 사용자 */
--tier-premium: #ffd700    /* 프리미엄 사용자 */
--tier-enterprise: #9b59b6 /* 엔터프라이즈 */

/* 알림 컬러 (추가) */
--alert-critical: #e74c3c  /* 긴급 알림 */
--alert-high: #f39c12      /* 높음 알림 */
--alert-medium: #3498db    /* 보통 알림 */
--alert-low: #2ecc71       /* 낮음 알림 */
```

## 8. 상태 관리 (확장)

### 8.1 Zustand 스토어
```typescript
// useAuthStore.ts (확장)
interface AuthStore {
  user: User | null;
  isAuthenticated: boolean;
  subscription: 'free' | 'premium' | 'enterprise';
  permissions: Permission[];
  login: (googleToken: string) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
  checkPermission: (permission: string) => boolean;
}

// useWAFStore.ts (확장)
interface WAFStore {
  status: 'active' | 'inactive' | 'error';
  stats: WAFStats;
  rules: Rule[];
  userRules: CustomRule[];
  toggleRule: (ruleId: string) => void;
  addCustomRule: (rule: CustomRule) => void;
  backupRules: () => Promise<void>;
  restoreRules: (backupId: string) => Promise<void>;
}

// useNotificationStore.ts (신규)
interface NotificationStore {
  alerts: Alert[];
  settings: NotificationSettings;
  addAlert: (alert: Alert) => void;
  markAsRead: (alertId: string) => void;
  updateSettings: (settings: Partial<NotificationSettings>) => void;
}
```

## 9. API 엔드포인트 설계

### 인증 관련
```typescript
POST /api/auth/google          # Google OAuth 로그인
POST /api/auth/refresh         # 토큰 갱신
POST /api/auth/logout          # 로그아웃
GET  /api/auth/me             # 현재 사용자 정보
GET  /api/auth/permissions    # 사용자 권한 목록
```

### 대시보드 관련
```typescript
GET  /api/dashboard/stats     # 대시보드 통계 요약
GET  /api/dashboard/traffic   # 실시간 트래픽 데이터
GET  /api/dashboard/attacks   # 최근 공격 목록
GET  /api/dashboard/geo       # 지리적 공격 분포
WS   /ws/realtime            # 실시간 데이터 스트림
```

### 룰 관리 관련
```typescript
GET    /api/rules                    # 사용자 룰 목록
POST   /api/rules                    # 새 룰 생성
PUT    /api/rules/:id                # 룰 수정
DELETE /api/rules/:id                # 룰 삭제
POST   /api/rules/test               # 룰 테스트
GET    /api/rules/:id/history        # 룰 변경 이력
POST   /api/rules/backup             # 룰 백업
POST   /api/rules/restore/:backupId  # 룰 복원
```

### 분석 관련
```typescript
GET  /api/analytics/trends           # 트렌드 분석
GET  /api/analytics/attacks          # 공격 유형별 통계
GET  /api/analytics/performance      # 성능 지표
POST /api/analytics/report           # 맞춤 보고서 생성
GET  /api/analytics/export/:format   # 데이터 내보내기
```

## 10. 브랜치 전략 (SaaS 확장)

### Frontend 브랜치
```
feat/console-fe-auth        # Google OAuth, 멀티테넌트 인증
feat/console-fe-dashboard   # 실시간 대시보드, 멀티유저 필터링
feat/console-fe-rules       # 룰 관리, 사용자별 네임스페이스
feat/console-fe-analytics   # 고급 분석, 맞춤 보고서
feat/console-fe-settings    # 시스템 설정, 알림 관리
feat/console-fe-profile     # 사용자 프로필, 구독 관리
feat/console-fe-admin       # 관리자 패널, 사용자 관리
```

### Backend 브랜치 (기존 + 확장)
```
feat/console-be-auth        # OAuth, JWT, 멀티테넌트 인증
feat/console-be-api         # REST API, 사용자별 데이터 격리
feat/console-be-rules       # 룰 관리, 네임스페이스
feat/console-be-analytics   # 분석 엔진, 리포팅
feat/console-be-websocket   # 실시간 데이터 스트리밍
```

## 11. 실시간 데이터 연동 (확장)

### WebSocket 연결 (멀티테넌트)
```typescript
// services/websocket.ts
class WAFWebSocket {
  private ws: WebSocket;
  private userId: string;
  
  connect(token: string) {
    this.ws = new WebSocket(`${WS_ENDPOINT}?token=${token}`);
    
    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      
      // 사용자별 데이터 필터링
      if (data.userId !== this.userId) return;
      
      switch (data.type) {
        case 'user_traffic_update':
          updateUserTrafficChart(data.payload);
          break;
        case 'user_attack_blocked':
          showPersonalAttackAlert(data.payload);
          break;
        case 'user_log_entry':
          addUserLog(data.payload);
          break;
        case 'system_alert':
          showSystemAlert(data.payload);
          break;
      }
    };
  }
}
```

## 12. 환불 조건 검증 (기존 유지)

### 자동 테스트 시나리오
- [ ] DVWA 정상 요청 통과 확인
- [ ] SQL Injection (`' OR '1'='1`) 차단 확인
- [ ] XSS (`<script>alert(1)</script>`) 차단 확인
- [ ] 악성 파일 업로드 (.php) 차단 확인
- [ ] Command Injection (`; ls`, `&& cat /etc/passwd`) 차단 확인
- [ ] LFI/RFI 경로조작 (`../../../etc/passwd`) 차단 확인

### 증거 수집 (사용자별)
- [ ] 403 차단 응답 스크린샷 자동 캡처
- [ ] 사용자별 WAF 로그 자동 수집
- [ ] ModSecurity 로그 파싱 및 개인화
- [ ] 개인 맞춤형 PDF 리포트 생성

## 13. 배포 전략

### Frontend 배포 (Vercel)
```json
// vercel.json (멀티환경)
{
  "framework": "vite",
  "env": {
    "VITE_API_URL": "@api_url",
    "VITE_WS_URL": "@ws_url", 
    "VITE_GOOGLE_CLIENT_ID": "@google_client_id",
    "VITE_ENVIRONMENT": "@environment"
  },
  "functions": {
    "src/pages/api/*.ts": {
      "maxDuration": 30
    }
  }
}
```

### 환경별 설정
```typescript
// 개발환경
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws

// 스테이징
VITE_API_URL=https://staging-api.waf-saas.com
VITE_WS_URL=wss://staging-api.waf-saas.com/ws

// 프로덕션
VITE_API_URL=https://api.waf-saas.com
VITE_WS_URL=wss://api.waf-saas.com/ws
```

## 14. 개발 우선순위 (SaaS 중심)

### Phase 1: 인증 및 멀티테넌트 기반
1. Google OAuth 2.0 연동
2. JWT 기반 사용자 인증
3. 사용자별 데이터 격리
4. 기본 라우팅 및 권한 체크

### Phase 2: 핵심 SaaS 기능
1. 사용자별 대시보드
2. 멀티테넌트 룰 관리
3. 실시간 데이터 스트리밍
4. 개인화된 알림 시스템

### Phase 3: 고급 SaaS 기능
1. 고급 분석 및 리포팅
2. 구독 플랜 관리
3. 관리자 패널
4. API 키 관리

### Phase 4: 운영 및 최적화
1. 모니터링 및 로깅
2. 성능 최적화
3. 보안 강화
4. 자동화된 테스트

---