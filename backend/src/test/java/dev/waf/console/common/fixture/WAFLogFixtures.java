package dev.waf.console.common.fixture;

import dev.waf.console.waflog.domain.WAFLog;
import dev.waf.console.waflog.domain.WAFLog.LogStatus;

import java.time.LocalDateTime;

/**
 * WAFLog 엔티티 테스트 픽스처
 *
 * 테스트에서 재사용 가능한 WAFLog 객체를 생성합니다.
 */
public class WAFLogFixtures {

    public static final String DEFAULT_SOURCE_IP = "192.168.1.100";
    public static final String DEFAULT_HTTP_METHOD = "GET";
    public static final String DEFAULT_REQUEST_URI = "/api/users";
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    public static final String DEFAULT_RULE_ID = "920100";
    public static final String DEFAULT_RULE_NAME = "SQL Injection Detection";

    /**
     * 기본 WAF 로그 생성 (SUCCESS 상태)
     */
    public static WAFLog createDefaultLog() {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.SUCCESS)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .requestUri(DEFAULT_REQUEST_URI)
            .userAgent(DEFAULT_USER_AGENT)
            .riskScore(10)
            .responseTimeMs(50L)
            .responseStatusCode(200)
            .payloadSize(1024L)
            .build();
    }

    /**
     * BLOCKED 상태의 로그 생성
     */
    public static WAFLog createBlockedLog() {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.BLOCKED)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod("POST")
            .requestUri("/api/admin/users")
            .userAgent(DEFAULT_USER_AGENT)
            .attackType("SQL_INJECTION")
            .riskScore(95)
            .ruleId(DEFAULT_RULE_ID)
            .ruleName(DEFAULT_RULE_NAME)
            .blockReason("SQL Injection pattern detected in query parameter")
            .responseTimeMs(25L)
            .responseStatusCode(403)
            .payloadSize(512L)
            .build();
    }

    /**
     * 특정 공격 유형의 로그 생성
     */
    public static WAFLog createLogWithAttackType(String attackType) {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.BLOCKED)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod("POST")
            .requestUri("/api/search")
            .userAgent(DEFAULT_USER_AGENT)
            .attackType(attackType)
            .riskScore(80)
            .ruleId(DEFAULT_RULE_ID)
            .ruleName(attackType + " Detection")
            .blockReason(attackType + " pattern detected")
            .responseTimeMs(30L)
            .responseStatusCode(403)
            .build();
    }

    /**
     * XSS 공격 로그 생성
     */
    public static WAFLog createXssLog() {
        return createLogWithAttackType("XSS");
    }

    /**
     * SQL Injection 공격 로그 생성
     */
    public static WAFLog createSqlInjectionLog() {
        return createLogWithAttackType("SQL_INJECTION");
    }

    /**
     * Path Traversal 공격 로그 생성
     */
    public static WAFLog createPathTraversalLog() {
        return createLogWithAttackType("PATH_TRAVERSAL");
    }

    /**
     * 특정 위험도 점수의 로그 생성
     */
    public static WAFLog createLogWithRiskScore(int riskScore) {
        LogStatus status = riskScore >= 70 ? LogStatus.BLOCKED :
                          riskScore >= 40 ? LogStatus.WARNING : LogStatus.SUCCESS;

        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .requestUri(DEFAULT_REQUEST_URI)
            .userAgent(DEFAULT_USER_AGENT)
            .riskScore(riskScore)
            .responseTimeMs(50L)
            .responseStatusCode(status == LogStatus.BLOCKED ? 403 : 200)
            .build();
    }

    /**
     * 고위험 로그 생성 (위험도 70 이상)
     */
    public static WAFLog createHighRiskLog() {
        return createLogWithRiskScore(90);
    }

    /**
     * 저위험 로그 생성 (위험도 30 이하)
     */
    public static WAFLog createLowRiskLog() {
        return createLogWithRiskScore(15);
    }

    /**
     * 특정 IP의 로그 생성
     */
    public static WAFLog createLogWithSourceIp(String sourceIp) {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.SUCCESS)
            .sourceIp(sourceIp)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .requestUri(DEFAULT_REQUEST_URI)
            .userAgent(DEFAULT_USER_AGENT)
            .riskScore(10)
            .responseTimeMs(50L)
            .responseStatusCode(200)
            .build();
    }

    /**
     * 특정 시간대의 로그 생성
     */
    public static WAFLog createLogWithTimestamp(LocalDateTime timestamp) {
        return WAFLog.builder()
            .timestamp(timestamp)
            .status(LogStatus.SUCCESS)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .requestUri(DEFAULT_REQUEST_URI)
            .userAgent(DEFAULT_USER_AGENT)
            .riskScore(10)
            .responseTimeMs(50L)
            .responseStatusCode(200)
            .build();
    }

    /**
     * ERROR 상태의 로그 생성
     */
    public static WAFLog createErrorLog() {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.ERROR)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .requestUri(DEFAULT_REQUEST_URI)
            .userAgent(DEFAULT_USER_AGENT)
            .riskScore(0)
            .responseTimeMs(100L)
            .responseStatusCode(500)
            .blockReason("Internal processing error")
            .build();
    }

    /**
     * WARNING 상태의 로그 생성
     */
    public static WAFLog createWarningLog() {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.WARNING)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod("POST")
            .requestUri("/api/upload")
            .userAgent(DEFAULT_USER_AGENT)
            .riskScore(50)
            .responseTimeMs(75L)
            .responseStatusCode(200)
            .blockReason("Suspicious file upload pattern")
            .build();
    }

    /**
     * 여러 로그 생성
     */
    public static WAFLog[] createMultipleLogs(int count) {
        WAFLog[] logs = new WAFLog[count];
        LogStatus[] statuses = LogStatus.values();

        for (int i = 0; i < count; i++) {
            LogStatus status = statuses[i % statuses.length];
            logs[i] = WAFLog.builder()
                .timestamp(LocalDateTime.now().minusMinutes(count - i))
                .status(status)
                .sourceIp("192.168.1." + (100 + i))
                .httpMethod(i % 2 == 0 ? "GET" : "POST")
                .requestUri("/api/resource/" + i)
                .userAgent(DEFAULT_USER_AGENT)
                .riskScore(i * 10)
                .responseTimeMs((long) (50 + i * 5))
                .responseStatusCode(status == LogStatus.BLOCKED ? 403 : 200)
                .build();
        }
        return logs;
    }

    /**
     * 완전히 커스터마이징된 로그 생성
     */
    public static WAFLog createCustomLog(
            LogStatus status,
            String sourceIp,
            String httpMethod,
            String requestUri,
            String attackType,
            Integer riskScore
    ) {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .sourceIp(sourceIp)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .userAgent(DEFAULT_USER_AGENT)
            .attackType(attackType)
            .riskScore(riskScore)
            .responseTimeMs(50L)
            .responseStatusCode(status == LogStatus.BLOCKED ? 403 : 200)
            .build();
    }

    /**
     * 지리적 위치 정보가 포함된 로그 생성
     */
    public static WAFLog createLogWithGeoLocation(String countryCode) {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.SUCCESS)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .requestUri(DEFAULT_REQUEST_URI)
            .userAgent(DEFAULT_USER_AGENT)
            .riskScore(10)
            .geoCountry(countryCode)
            .responseTimeMs(50L)
            .responseStatusCode(200)
            .build();
    }

    /**
     * 세션 ID가 포함된 로그 생성
     */
    public static WAFLog createLogWithSession(String sessionId) {
        return WAFLog.builder()
            .timestamp(LocalDateTime.now())
            .status(LogStatus.SUCCESS)
            .sourceIp(DEFAULT_SOURCE_IP)
            .httpMethod(DEFAULT_HTTP_METHOD)
            .requestUri(DEFAULT_REQUEST_URI)
            .userAgent(DEFAULT_USER_AGENT)
            .sessionId(sessionId)
            .riskScore(10)
            .responseTimeMs(50L)
            .responseStatusCode(200)
            .build();
    }
}
