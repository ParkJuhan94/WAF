package dev.waf.console.waflog.domain;

import dev.waf.console.common.fixture.TestFixtures;
import dev.waf.console.common.fixture.WAFLogFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WAFLog 도메인 테스트
 *
 * TestFixtures를 사용한 도메인 로직 테스트 예시
 */
@DisplayName("WAFLog 도메인 테스트")
class WAFLogTest implements TestFixtures {

    @Test
    @DisplayName("기본 로그 생성 및 검증")
    void createDefaultLog() {
        // given & when: 기본 로그 생성
        WAFLog log = logs().createDefaultLog();

        // then: 로그 상태 검증
        assertThat(log).isNotNull();
        assertThat(log.getStatus()).isEqualTo(WAFLog.LogStatus.SUCCESS);
        assertThat(log.isSuccess()).isTrue();
        assertThat(log.isBlocked()).isFalse();
    }

    @Test
    @DisplayName("차단된 로그 생성 및 검증")
    void createBlockedLog() {
        // given & when: 차단된 로그 생성
        WAFLog blockedLog = logs().createBlockedLog();

        // then: 차단 상태 검증
        assertThat(blockedLog.getStatus()).isEqualTo(WAFLog.LogStatus.BLOCKED);
        assertThat(blockedLog.isBlocked()).isTrue();
        assertThat(blockedLog.isSuccess()).isFalse();
        assertThat(blockedLog.getAttackType()).isNotNull();
        assertThat(blockedLog.getBlockReason()).isNotBlank();
        assertThat(blockedLog.getRiskScore()).isGreaterThan(70);
    }

    @Test
    @DisplayName("XSS 공격 로그 검증")
    void verifyXssLog() {
        // given & when: XSS 로그 생성
        WAFLog xssLog = logs().createXssLog();

        // then: XSS 로그 특성 검증
        assertThat(xssLog.getAttackType()).isEqualTo("XSS");
        assertThat(xssLog.getStatus()).isEqualTo(WAFLog.LogStatus.BLOCKED);
        assertThat(xssLog.getRiskScore()).isGreaterThanOrEqualTo(70);
    }

    @Test
    @DisplayName("SQL Injection 공격 로그 검증")
    void verifySqlInjectionLog() {
        // given & when: SQL Injection 로그 생성
        WAFLog sqlLog = logs().createSqlInjectionLog();

        // then: SQL Injection 로그 특성 검증
        assertThat(sqlLog.getAttackType()).isEqualTo("SQL_INJECTION");
        assertThat(sqlLog.isBlocked()).isTrue();
    }

    @Test
    @DisplayName("고위험 로그 판별")
    void identifyHighRiskLog() {
        // given & when: 고위험 로그 생성
        WAFLog highRiskLog = logs().createHighRiskLog();

        // then: 고위험 로그 확인
        assertThat(highRiskLog.isHighRisk()).isTrue();
        assertThat(highRiskLog.getRiskScore()).isGreaterThanOrEqualTo(70);
    }

    @Test
    @DisplayName("저위험 로그는 고위험으로 판별되지 않음")
    void lowRiskLogIsNotHighRisk() {
        // given & when: 저위험 로그 생성
        WAFLog lowRiskLog = WAFLogFixtures.createLowRiskLog();

        // then: 고위험이 아님을 확인
        assertThat(lowRiskLog.isHighRisk()).isFalse();
        assertThat(lowRiskLog.getRiskScore()).isLessThan(70);
    }

    @Test
    @DisplayName("여러 로그 생성 및 상태 분포 확인")
    void verifyMultipleLogsDistribution() {
        // given & when: 여러 로그 생성
        WAFLog[] logs = logs().createMultipleLogs(10);

        // then: 로그 생성 확인
        assertThat(logs).hasSize(10);

        // 다양한 상태의 로그가 포함되어 있는지 확인
        long blockedCount = java.util.Arrays.stream(logs)
            .filter(WAFLog::isBlocked)
            .count();

        long successCount = java.util.Arrays.stream(logs)
            .filter(WAFLog::isSuccess)
            .count();

        assertThat(blockedCount + successCount).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("로그 빌더 패턴 검증")
    void verifyLogBuilderPattern() {
        // given & when: 빌더로 커스텀 로그 생성
        WAFLog customLog = WAFLog.builder()
            .status(WAFLog.LogStatus.WARNING)
            .sourceIp("10.0.0.1")
            .httpMethod("POST")
            .requestUri("/api/sensitive")
            .riskScore(65)
            .attackType("CUSTOM_ATTACK")
            .build();

        // then: 커스텀 설정 확인
        assertThat(customLog.getStatus()).isEqualTo(WAFLog.LogStatus.WARNING);
        assertThat(customLog.getSourceIp()).isEqualTo("10.0.0.1");
        assertThat(customLog.getHttpMethod()).isEqualTo("POST");
        assertThat(customLog.getRiskScore()).isEqualTo(65);
        assertThat(customLog.getAttackType()).isEqualTo("CUSTOM_ATTACK");
    }

    @Test
    @DisplayName("로그 상태별 생성 확인")
    void verifyLogsByStatus() {
        // given & when: 다양한 상태의 로그 생성
        WAFLog successLog = logs().createDefaultLog();
        WAFLog blockedLog = logs().createBlockedLog();
        WAFLog errorLog = WAFLogFixtures.createErrorLog();
        WAFLog warningLog = WAFLogFixtures.createWarningLog();

        // then: 각 로그의 상태 확인
        assertThat(successLog.getStatus()).isEqualTo(WAFLog.LogStatus.SUCCESS);
        assertThat(blockedLog.getStatus()).isEqualTo(WAFLog.LogStatus.BLOCKED);
        assertThat(errorLog.getStatus()).isEqualTo(WAFLog.LogStatus.ERROR);
        assertThat(warningLog.getStatus()).isEqualTo(WAFLog.LogStatus.WARNING);
    }

    @Test
    @DisplayName("공격 타입별 로그 검증")
    void verifyAttackTypeLog() {
        // given & when: 특정 공격 타입 로그 생성
        WAFLog pathTraversalLog = WAFLogFixtures.createPathTraversalLog();

        // then: 공격 타입 확인
        assertThat(pathTraversalLog.getAttackType()).isEqualTo("PATH_TRAVERSAL");
        assertThat(pathTraversalLog.isBlocked()).isTrue();
    }
}
