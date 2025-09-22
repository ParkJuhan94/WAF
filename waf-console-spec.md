# WAF 콘솔 프론트엔드 명세서

## 1. 프로젝트 개요

### 목표
ModSecurity + OWASP CRS 기반 WAF 관리를 위한 **단일 페이지 웹 콘솔** 개발

### 핵심 요구사항
- **환불 조건 대응**: DVWA 정상 요청 통과, 5가지 공격 유형 차단 증명
- **실시간 트래픽 모니터링**: 차트 기반 실시간 데이터 시각화
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

### 배포
- **Vercel** (무료, Git 연동 자동 배포, CDN)
- **환경변수**: Vercel 환경변수로 API 엔드포인트 관리

## 3. 디자인 시스템

### 컬러 팔레트 (다크 테마)
```css
/* 메인 컬러 */
--bg-primary: #1b2431      /* 메인 배경 */
--accent-primary: #1ec997   /* 포인트 컬러 (성공, 활성화) */

/* 상태 컬러 */
--danger: #ff6b6b          /* 위험, 차단 */
--warning: #feca57         /* 경고, 탐지 */
--info: #48cae4            /* 정보 */
--success: #1ec997         /* 성공, 정상 */

/* UI 컬러 */
--bg-card: #242c3a         /* 카드 배경 */
--bg-surface: #2c3545      /* 표면 배경 */
--text-primary: #ffffff    /* 기본 텍스트 */
--text-secondary: #a8b2c1  /* 보조 텍스트 */
--border: #3a4553          /* 테두리 */
```

### 상태 표시 시스템
```javascript
// WAF 상태
Active: { color: success, icon: "check-circle", animation: "pulse" }
Blocked: { color: danger, icon: "stop", animation: "blink" }
Warning: { color: warning, icon: "exclamation-triangle" }
Monitoring: { color: info, icon: "eye" }

// 공격 심각도
Critical: { color: danger, badge: "위험", animation: "blink" }
High: { color: warning, badge: "높음" }
Medium: { color: info, badge: "보통" }
Low: { color: success, badge: "낮음" }
```

## 4. 프로젝트 구조

```
waf-console/
├── src/
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppLayout.tsx       # 전체 레이아웃
│   │   │   ├── Header.tsx          # 헤더 (사용자 정보, 상태)
│   │   │   └── Sidebar.tsx         # 사이드바 네비게이션
│   │   ├── dashboard/
│   │   │   ├── TrafficChart.tsx    # 실시간 트래픽 차트
│   │   │   ├── StatsCards.tsx      # 통계 카드 그리드
│   │   │   ├── RecentAttacks.tsx   # 최근 공격 피드
│   │   │   ├── SystemStatus.tsx    # WAF 시스템 상태
│   │   │   └── QuickActions.tsx    # 빠른 액션 버튼
│   │   ├── logs/
│   │   │   ├── LogViewer.tsx       # 로그 뷰어 (검색, 필터)
│   │   │   ├── LogDetail.tsx       # 로그 상세 모달
│   │   │   └── AttackEvidence.tsx  # 차단 증거 수집
│   │   ├── rules/
│   │   │   ├── RuleManager.tsx     # 룰 관리 패널
│   │   │   ├── RuleEditor.tsx      # 룰 에디터 모달
│   │   │   └── RuleTester.tsx      # 룰 테스트 도구
│   │   ├── whitelist/
│   │   │   ├── WhitelistPanel.tsx  # 화이트리스트 관리
│   │   │   └── IPManager.tsx       # IP 추가/삭제
│   │   ├── testing/
│   │   │   ├── AttackSimulator.tsx # 공격 시뮬레이터
│   │   │   ├── DVWAIntegration.tsx # DVWA 테스트 연동
│   │   │   └── TestReport.tsx      # 환불 조건 검증 리포트
│   │   ├── auth/
│   │   │   └── GoogleLogin.tsx     # 구글 소셜 로그인
│   │   └── common/
│   │       ├── Loading.tsx
│   │       ├── ErrorBoundary.tsx
│   │       └── StatusBadge.tsx     # 상태 표시 공통 컴포넌트
│   ├── stores/
│   │   ├── useAuthStore.ts         # 인증 상태
│   │   ├── useWAFStore.ts          # WAF 상태
│   │   └── useLogStore.ts          # 로그 필터 상태
│   ├── hooks/
│   │   ├── useRealtimeData.ts      # 실시간 데이터 훅
│   │   ├── useAttackSimulator.ts   # 공격 시뮬레이션 훅
│   │   └── useLogExporter.ts       # 로그 내보내기 훅
│   ├── services/
│   │   ├── api.ts                  # API 클라이언트
│   │   ├── websocket.ts           # WebSocket 연결
│   │   └── attackTests.ts         # 공격 테스트 시나리오
│   ├── types/
│   │   ├── api.ts                  # API 타입 정의
│   │   ├── waf.ts                  # WAF 관련 타입
│   │   └── logs.ts                 # 로그 관련 타입
│   ├── utils/
│   │   ├── formatters.ts           # 데이터 포매팅
│   │   ├── chartConfig.ts          # 차트 설정
│   │   └── constants.ts            # 상수 정의
│   ├── App.tsx                     # 메인 앱 컴포넌트
│   └── main.tsx                    # 엔트리 포인트
├── public/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js              # Ant Design 커스터마이징
└── vercel.json                     # Vercel 배포 설정
```

## 5. 페이지 구성 (Single Page App)

### 메인 대시보드 (단일 페이지)
```typescript
// 레이아웃 구조
<AppLayout>
  <Header />
  <Sidebar />
  <MainContent>
    <Row>
      <Col span={16}>
        <TrafficChart />        // 실시간 트래픽 차트
        <RecentAttacks />       // 최근 공격 로그
      </Col>
      <Col span={8}>
        <SystemStatus />        // WAF 상태
        <StatsCards />          // 통계 카드
        <QuickActions />        // 빠른 액션
      </Col>
    </Row>
    
    <Tabs>
      <Tab key="logs">로그 분석</Tab>
      <Tab key="rules">룰 관리</Tab>
      <Tab key="whitelist">화이트리스트</Tab>
      <Tab key="testing">공격 테스트</Tab>
      <Tab key="settings">설정</Tab>
    </Tabs>
    
    <TabContent />
  </MainContent>
</AppLayout>
```

## 6. 핵심 기능 상세

### 6.1 실시간 트래픽 모니터링
```typescript
// TrafficChart.tsx 주요 기능
- 실시간 요청 수 (정상 vs 차단)
- 시간대별 트래픽 패턴 (라인 차트)
- 공격 유형별 분포 (파이 차트)
- WebSocket으로 1초마다 업데이트
- 차트 확대/축소, 시간 범위 선택
```

### 6.2 환불 조건 대응 기능
```typescript
// AttackSimulator.tsx
const ATTACK_SCENARIOS = {
  sqlInjection: "' OR '1'='1",
  xss: "<script>alert(1)</script>",
  fileUpload: "test.php",
  commandInjection: "; ls && cat /etc/passwd",
  pathTraversal: "../../../etc/passwd"
}

// 자동 테스트 실행
- DVWA 대상 정상 요청 테스트
- 5가지 공격 시나리오 자동 실행
- 차단 응답(403) 스크린샷 자동 캡처
- WAF 로그 자동 수집
- PDF 증거 리포트 생성
```

### 6.3 로그 분석
```typescript
// LogViewer.tsx
- 실시간 로그 스트리밍 (WebSocket)
- 고급 필터링 (IP, 시간, 공격 유형, 심각도)
- 로그 상세보기 (요청 헤더, 페이로드, 매칭 룰)
- 로그 내보내기 (CSV, JSON, PDF)
- 공격 패턴 하이라이팅
```

### 6.4 룰 관리
```typescript
// RuleManager.tsx
- OWASP CRS 룰 목록 (활성화/비활성화)
- 커스텀 룰 CRUD (ModSecurity 문법 지원)
- 룰 우선순위 드래그앤드롭
- 룰 테스트 도구 (가상 요청 시뮬레이션)
- 룰 적용 즉시 반영 (무중단 리로드)
```

## 7. 상태 관리

### 7.1 Zustand 스토어
```typescript
// useAuthStore.ts
interface AuthStore {
  user: User | null;
  isAuthenticated: boolean;
  login: (googleToken: string) => Promise<void>;
  logout: () => void;
}

// useWAFStore.ts
interface WAFStore {
  status: 'active' | 'inactive' | 'error';
  stats: WAFStats;
  rules: Rule[];
  toggleRule: (ruleId: string) => void;
  addCustomRule: (rule: CustomRule) => void;
}
```

### 7.2 React Query 설정
```typescript
// 실시간 데이터 페칭
const { data: trafficData } = useQuery({
  queryKey: ['traffic'],
  queryFn: fetchTrafficData,
  refetchInterval: 1000, // 1초마다 업데이트
});

const { data: logs } = useQuery({
  queryKey: ['logs', filters],
  queryFn: ({ queryKey }) => fetchLogs(queryKey[1]),
  refetchInterval: 2000,
});
```

## 8. 실시간 데이터 연동

### WebSocket 연결
```typescript
// services/websocket.ts
class WAFWebSocket {
  private ws: WebSocket;
  
  connect() {
    this.ws = new WebSocket(WS_ENDPOINT);
    
    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      
      switch (data.type) {
        case 'traffic_update':
          updateTrafficChart(data.payload);
          break;
        case 'attack_blocked':
          showAttackAlert(data.payload);
          break;
        case 'log_entry':
          addNewLog(data.payload);
          break;
      }
    };
  }
}
```

## 9. 배포 설정

### 9.1 Vercel 배포
```json
// vercel.json
{
  "framework": "vite",
  "buildCommand": "npm run build",
  "outputDirectory": "dist",
  "routes": [
    { "handle": "filesystem" },
    { "src": "/.*", "dest": "/index.html" }
  ],
  "env": {
    "VITE_API_URL": "@api_url",
    "VITE_WS_URL": "@ws_url",
    "VITE_GOOGLE_CLIENT_ID": "@google_client_id"
  }
}
```

### 9.2 환경변수 설정
```typescript
// .env.example
VITE_API_URL=https://waf-api.example.com
VITE_WS_URL=wss://waf-api.example.com/ws
VITE_GOOGLE_CLIENT_ID=your_google_client_id

// .env.local (개발용)
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

### 9.3 배포 자동화
```bash
# GitHub Actions (선택사항)
# Vercel은 Git push만으로 자동 배포
git add .
git commit -m "feat: WAF console frontend"
git push origin main

# Vercel 자동 배포 실행
# 배포 URL: https://waf-console.vercel.app
```

## 10. 개발 가이드

### 10.1 프로젝트 초기화
```bash
npm create vite@latest waf-console -- --template react-ts
cd waf-console
npm install antd @ant-design/icons zustand @tanstack/react-query
npm install echarts react-echarts-v5 axios
npm install -D @types/node tailwindcss
```

### 10.2 개발 서버 실행
```bash
npm run dev
# 개발 서버: http://localhost:5173
```

### 10.3 빌드 및 미리보기
```bash
npm run build
npm run preview
# 미리보기: http://localhost:4173
```

## 11. 품질 가드

### 성능 최적화
- **Code Splitting**: React.lazy로 탭별 컴포넌트 지연 로딩
- **메모화**: React.memo, useMemo로 불필요한 리렌더링 방지
- **차트 최적화**: 대용량 데이터는 페이지네이션, 가상화 적용

### 보안 고려사항
- **XSS 방지**: Ant Design 기본 보안, 사용자 입력 검증
- **환경변수**: API 키 등 민감 정보는 Vercel 환경변수로 관리
- **HTTPS**: Vercel 자동 SSL 인증서

### 장애 시나리오
- **WebSocket 연결 실패**: 자동 재연결 로직, 폴링 방식 Fallback
- **API 서버 다운**: Error Boundary, 에러 상태 표시
- **대용량 로그**: Virtual Scrolling, 페이지네이션으로 메모리 보호

## 12. 환불 조건 검증 체크리스트

### 자동 테스트 시나리오
- [ ] DVWA 정상 요청 통과 확인
- [ ] SQL Injection (`' OR '1'='1`) 차단 확인
- [ ] XSS (`<script>alert(1)</script>`) 차단 확인
- [ ] 악성 파일 업로드 (.php) 차단 확인
- [ ] Command Injection (`; ls`, `&& cat /etc/passwd`) 차단 확인
- [ ] LFI/RFI 경로조작 (`../../../etc/passwd`) 차단 확인

### 증거 수집
- [ ] 403 차단 응답 스크린샷 자동 캡처
- [ ] WAF 로그 자동 수집 및 저장
- [ ] ModSecurity 로그 파싱 및 표시
- [ ] 환불 심사용 PDF 리포트 생성

---