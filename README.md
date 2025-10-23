# 🔐 WAF

```
🧑🏻‍💻 [브라우저 또는 curl]
     ↓
[WAF Docker 컨테이너 :80]
  ↳ Nginx + ModSecurity
     ↓
[Spring Boot 앱 :8080]
```

- 웹 애플리케이션 방화벽(`Web Application Firewall`)의 약자로, 웹 서버 앞단에 위치하여 웹 애플리케이션을 공격으로부터 보호하는 보안 솔루션입니다.
- `HTTP/HTTPS 프로토콜`을 통해 전송되는 트래픽을 분석하고 악성 공격을 차단하여 웹 서비스의 안전성을 높입니다.
- `nginx` + `ModSecurity` + `OWASP CRS` 기반의 WAF를 직접 개발하고, `SaaS 형태`로 구조화합니다.

## ⚙️ 환경 설정

### 백엔드 환경변수 설정

1. **`.env` 파일 생성**
   ```bash
   cd backend
   cp .env.example .env
   ```

2. **필수 환경변수 설정**
   ```bash
   # 데이터베이스
   DB_PASSWORD=your-password-here

   # Google OAuth (https://console.cloud.google.com/)
   GOOGLE_CLIENT_ID=your-google-client-id-here
   GOOGLE_CLIENT_SECRET=your-google-client-secret-here

   # JWT Secret (openssl rand -base64 32 로 생성)
   JWT_SECRET=your-256-bit-secret-key
   ```

3. **선택적 환경변수** (필요 시 수정)
   - `REDIS_HOST`, `REDIS_PORT`: Redis 연결 정보
   - `KAFKA_ENABLED=true`: Kafka 사용 시
   - `ELASTICSEARCH_ENABLED=true`: Elasticsearch 사용 시

### 서비스 구성
- **기본 서비스** (항상 실행)
  - `waf`: WAF 컨테이너 (Nginx + ModSecurity)
  - `web`: 백엔드 웹 서버 (테스트용)
  - `mysql`: MySQL 데이터베이스
  - `redis`: Redis 캐시

- **모니터링 서비스**
  - `zookeeper`: Kafka Zookeeper
  - `kafka`: Kafka 메시지 브로커
  - `elasticsearch`: Elasticsearch 검색 엔진
