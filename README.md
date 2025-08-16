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

## 3주차 – 커스텀 룰 작성 및 최적화

- 시나리오 기반의 ModSecurity 룰을 `docker/modsecurity/custom_rules.conf`에 추가하여
  SQL injection, XSS, 악성 User-Agent 등을 탐지합니다.
- `modsec-logs/before_optimization.log`과 `modsec-logs/after_optimization.log`는
  룰 최적화 전후의 탐지 로그 샘플입니다.
