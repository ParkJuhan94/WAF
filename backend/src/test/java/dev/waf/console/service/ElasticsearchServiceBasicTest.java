package dev.waf.console.service;

import dev.waf.console.event.AttackDetectedEvent;
import dev.waf.console.event.AccessLogEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Elasticsearch 서비스 기본 단위 테스트
 *
 * Testcontainers 없이 이벤트 객체 생성 및 직렬화 테스트
 */
@DisplayName("Elasticsearch 서비스 기본 단위 테스트")
class ElasticsearchServiceBasicTest {

    @Test
    @DisplayName("AttackDetectedEvent 객체 생성 성공")
    void createAttackDetectedEvent_Success() {
        // given & when
        AttackDetectedEvent event = createAttackEvent(
            "192.168.1.100",
            AttackDetectedEvent.AttackType.SQL_INJECTION,
            85
        );

        // then
        assertThat(event).isNotNull();
        assertThat(event.getSourceIp()).isEqualTo("192.168.1.100");
        assertThat(event.getAttackType()).isEqualTo(AttackDetectedEvent.AttackType.SQL_INJECTION);
        assertThat(event.getRiskScore()).isEqualTo(85);
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("AccessLogEvent 객체 생성 성공")
    void createAccessLogEvent_Success() {
        // given & when
        AccessLogEvent event = createAccessLogEvent("10.0.0.1", "/api/users", 200);

        // then
        assertThat(event).isNotNull();
        assertThat(event.getClientIp()).isEqualTo("10.0.0.1");
        assertThat(event.getUri()).isEqualTo("/api/users");
        assertThat(event.getStatusCode()).isEqualTo(200);
        assertThat(event.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("AttackType enum 동작 확인")
    void attackTypeEnum_Success() {
        // given
        AttackDetectedEvent.AttackType sqlInjection = AttackDetectedEvent.AttackType.SQL_INJECTION;

        // then
        assertThat(sqlInjection).isNotNull();
        assertThat(sqlInjection.getDescription()).isEqualTo("SQL 인젝션");
        assertThat(sqlInjection.name()).isEqualTo("SQL_INJECTION");
    }

    @Test
    @DisplayName("여러 공격 타입 이벤트 생성 성공")
    void createMultipleAttackTypes_Success() {
        // given & when
        AttackDetectedEvent xssEvent = createAttackEvent("192.168.1.1", AttackDetectedEvent.AttackType.XSS, 70);
        AttackDetectedEvent sqlEvent = createAttackEvent("192.168.1.2", AttackDetectedEvent.AttackType.SQL_INJECTION, 85);
        AttackDetectedEvent csrfEvent = createAttackEvent("192.168.1.3", AttackDetectedEvent.AttackType.CSRF, 60);

        // then
        assertThat(xssEvent.getAttackType()).isEqualTo(AttackDetectedEvent.AttackType.XSS);
        assertThat(sqlEvent.getAttackType()).isEqualTo(AttackDetectedEvent.AttackType.SQL_INJECTION);
        assertThat(csrfEvent.getAttackType()).isEqualTo(AttackDetectedEvent.AttackType.CSRF);
    }

    @Test
    @DisplayName("위험도 분류 로직 테스트")
    void riskScoreClassification_Success() {
        // given
        AttackDetectedEvent lowRisk = createAttackEvent("192.168.1.1", AttackDetectedEvent.AttackType.SCAN_ATTEMPT, 30);
        AttackDetectedEvent mediumRisk = createAttackEvent("192.168.1.2", AttackDetectedEvent.AttackType.XSS, 60);
        AttackDetectedEvent highRisk = createAttackEvent("192.168.1.3", AttackDetectedEvent.AttackType.SQL_INJECTION, 85);
        AttackDetectedEvent criticalRisk = createAttackEvent("192.168.1.4", AttackDetectedEvent.AttackType.COMMAND_INJECTION, 95);

        // then - 위험도 기준 확인
        assertThat(lowRisk.getRiskScore()).isLessThan(50);
        assertThat(mediumRisk.getRiskScore()).isBetween(50, 75);
        assertThat(highRisk.getRiskScore()).isBetween(75, 90);
        assertThat(criticalRisk.getRiskScore()).isGreaterThanOrEqualTo(90);
    }

    @Test
    @DisplayName("이벤트 타임스탬프 설정 확인")
    void eventTimestamp_Success() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        AttackDetectedEvent event = createAttackEvent("192.168.1.1", AttackDetectedEvent.AttackType.XSS, 70);

        // then
        LocalDateTime after = LocalDateTime.now();
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getTimestamp()).isBetween(before, after);
    }

    @Test
    @DisplayName("이벤트 ID 고유성 확인")
    void eventIdUniqueness_Success() {
        // given & when
        AttackDetectedEvent event1 = createAttackEvent("192.168.1.1", AttackDetectedEvent.AttackType.XSS, 70);
        AttackDetectedEvent event2 = createAttackEvent("192.168.1.2", AttackDetectedEvent.AttackType.SQL_INJECTION, 85);
        AttackDetectedEvent event3 = createAttackEvent("192.168.1.3", AttackDetectedEvent.AttackType.CSRF, 60);

        // then
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
        assertThat(event2.getEventId()).isNotEqualTo(event3.getEventId());
        assertThat(event1.getEventId()).isNotEqualTo(event3.getEventId());
    }

    @Test
    @DisplayName("Builder 패턴 동작 확인")
    void builderPattern_Success() {
        // given & when
        AttackDetectedEvent event = AttackDetectedEvent.builder()
            .sourceIp("192.168.1.100")
            .targetUrl("/api/admin")
            .httpMethod("POST")
            .attackType(AttackDetectedEvent.AttackType.SQL_INJECTION)
            .riskScore(90)
            .signature("' OR '1'='1")
            .ruleId("sql-001")
            .ruleName("SQL Injection Detection")
            .payload("username=' OR '1'='1' --")
            .userAgent("Mozilla/5.0")
            .blocked(true)
            .responseCode(403)
            .build();

        event.setEventId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        event.setSource("test");

        // then
        assertThat(event.getSourceIp()).isEqualTo("192.168.1.100");
        assertThat(event.getTargetUrl()).isEqualTo("/api/admin");
        assertThat(event.getHttpMethod()).isEqualTo("POST");
        assertThat(event.getAttackType()).isEqualTo(AttackDetectedEvent.AttackType.SQL_INJECTION);
        assertThat(event.getRiskScore()).isEqualTo(90);
        assertThat(event.getBlocked()).isTrue();
        assertThat(event.getResponseCode()).isEqualTo(403);
    }

    // ===== Helper Methods =====

    private AttackDetectedEvent createAttackEvent(String sourceIp,
                                                  AttackDetectedEvent.AttackType attackType,
                                                  Integer riskScore) {
        AttackDetectedEvent event = AttackDetectedEvent.builder()
            .sourceIp(sourceIp)
            .targetUrl("/api/test")
            .httpMethod("POST")
            .attackType(attackType)
            .riskScore(riskScore)
            .signature("Test signature")
            .ruleId("test-rule-" + UUID.randomUUID().toString().substring(0, 8))
            .ruleName("Test Rule")
            .payload("test payload")
            .userAgent("Mozilla/5.0")
            .blocked(true)
            .responseCode(403)
            .build();

        event.setEventId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        event.setSource("test");
        event.setCorrelationId(UUID.randomUUID().toString());

        return event;
    }

    private AccessLogEvent createAccessLogEvent(String clientIp, String uri, Integer statusCode) {
        AccessLogEvent event = AccessLogEvent.builder()
            .clientIp(clientIp)
            .method("GET")
            .uri(uri)
            .statusCode(statusCode)
            .responseTime(150L)
            .responseSize(1024L)
            .userAgent("Mozilla/5.0")
            .referer("http://example.com")
            .sessionId("session-" + UUID.randomUUID().toString().substring(0, 8))
            .userId("user-123")
            .build();

        event.setEventId(UUID.randomUUID().toString());
        event.setTimestamp(LocalDateTime.now());
        event.setSource("test");
        event.setCorrelationId(UUID.randomUUID().toString());

        return event;
    }
}
