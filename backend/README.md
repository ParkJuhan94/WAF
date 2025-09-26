# WAF 백엔드

Spring Boot 기반의 WAF 환불 조건 검증 API 서버입니다.

## 🚀 빠른 시작

### 1. 설정 파일 준비

```bash
# 메인 설정 파일 복사 및 수정
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 테스트 설정 파일 복사 (필요시)
cp src/test/resources/application-test.yml.example src/test/resources/application-test.yml
```

### 2. 데이터베이스 설정

`application.yml`에서 MySQL 연결 정보를 수정하세요:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/waf
    username: your_username
    password: your_password
```

### 3. OAuth2 설정 (선택사항)

Google OAuth2를 사용하려면 `application.yml`에서 설정하세요:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-google-client-id
            client-secret: your-google-client-secret
```

### 4. 애플리케이션 실행

```bash
# 개발 모드 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew build
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

## 📁 프로젝트 구조

```
backend/
├── src/main/java/dev/waf/console/
│   ├── api/refund/              # 환불 검증 API
│   ├── core/domain/simulation/  # 도메인 모델
│   ├── core/repository/         # 데이터 접근 계층
│   └── infrastructure/          # 인프라스트럭처
├── src/main/resources/
│   ├── application.yml.example  # 설정 파일 템플릿
│   └── ...
└── src/test/                    # 테스트 코드
```

## 🎯 주요 기능

### 환불 조건 검증 API

- **5가지 공격 유형 차단 테스트**
  - SQL Injection
  - XSS (Cross-Site Scripting)
  - File Upload
  - Command Injection
  - Path Traversal

- **DVWA 정상 요청 통과 테스트**
- **비동기 처리 및 결과 캐싱**
- **PDF 리포트 생성**
- **자동 스크린샷 캡처**

### API 엔드포인트

```http
POST /api/v1/refund/validate-conditions
GET  /api/v1/refund/validate-conditions/{batchId}
```

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "dev.waf.console.api.refund.*"
```

## 🔧 개발 환경

- **Java**: 17+
- **Spring Boot**: 3.5.4
- **Database**: MySQL 8.4+
- **Build Tool**: Gradle 8.14+

## 📝 설정 파일 주의사항

⚠️ **중요**: `application.yml` 파일들은 민감한 정보가 포함되어 Git에서 제외됩니다.

- 실제 설정은 `.example` 파일을 복사해서 사용하세요
- 데이터베이스 비밀번호, OAuth2 키 등은 환경변수 사용을 권장합니다

```bash
# 환경변수 예시
export DB_PASSWORD=your_secure_password
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
```