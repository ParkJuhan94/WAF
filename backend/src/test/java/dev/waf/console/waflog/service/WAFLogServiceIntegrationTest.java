package dev.waf.console.waflog.service;

import dev.waf.console.config.AbstractIntegrationTest;
import dev.waf.console.waflog.domain.WAFLog;
import dev.waf.console.waflog.repository.WAFLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WAFLog 서비스 통합 테스트")
class WAFLogServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WAFLogService wafLogService;

    @Autowired
    private WAFLogRepository wafLogRepository;

    @BeforeEach
    void setUp() {
        wafLogRepository.deleteAll();
    }

    @Test
    @DisplayName("WAF 로그 저장 및 조회 성공")
    void saveAndRetrieveLogs_Success() throws Exception {
        // given
        WAFLog log1 = createTestLog("192.168.1.1", "/api/test", WAFLog.LogStatus.SUCCESS);
        WAFLog log2 = createTestLog("192.168.1.2", "/api/admin", WAFLog.LogStatus.BLOCKED);

        // when
        CompletableFuture<WAFLog> future1 = wafLogService.saveLogAsync(log1);
        CompletableFuture<WAFLog> future2 = wafLogService.saveLogAsync(log2);

        CompletableFuture.allOf(future1, future2).join();

        Page<WAFLog> recentLogs = wafLogService.getRecentLogs(10);

        // then
        assertThat(recentLogs.getContent()).hasSize(2);
        assertThat(recentLogs.getContent())
            .extracting(WAFLog::getSourceIp)
            .containsExactlyInAnyOrder("192.168.1.1", "192.168.1.2");
    }

    @Test
    @DisplayName("상태별 로그 조회 성공")
    void getLogsByStatus_Success() throws Exception {
        // given
        wafLogService.saveLog(createTestLog("192.168.1.1", "/api/test", WAFLog.LogStatus.SUCCESS));
        wafLogService.saveLog(createTestLog("192.168.1.2", "/api/admin", WAFLog.LogStatus.BLOCKED));
        wafLogService.saveLog(createTestLog("192.168.1.3", "/api/user", WAFLog.LogStatus.BLOCKED));

        // when
        Page<WAFLog> blockedLogs = wafLogService.getRecentBlockedLogs(10);
        Page<WAFLog> successLogs = wafLogService.getRecentSuccessLogs(10);

        // then
        assertThat(blockedLogs.getContent()).hasSize(2);
        assertThat(successLogs.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("공격 탐지 로그 저장 성공")
    void logAttackDetection_Success() throws Exception {
        // when
        CompletableFuture<WAFLog> future = wafLogService.logAttackDetection(
            "192.168.1.100",
            "POST",
            "/api/login",
            "Mozilla/5.0",
            "SQL_INJECTION",
            85,
            "rule-1001",
            "SQL Injection Detection Rule",
            "Suspicious SQL pattern detected"
        );

        WAFLog savedLog = future.get();

        // then
        assertThat(savedLog).isNotNull();
        assertThat(savedLog.getStatus()).isEqualTo(WAFLog.LogStatus.BLOCKED);
        assertThat(savedLog.getAttackType()).isEqualTo("SQL_INJECTION");
        assertThat(savedLog.getRiskScore()).isEqualTo(85);
        assertThat(savedLog.isHighRisk()).isTrue();
    }

    @Test
    @DisplayName("높은 위험도 로그 조회 성공")
    void getHighRiskLogs_Success() {
        // given
        WAFLog lowRiskLog = createTestLog("192.168.1.1", "/api/test", WAFLog.LogStatus.WARNING);
        lowRiskLog.setRiskScore(30);

        WAFLog highRiskLog1 = createTestLog("192.168.1.2", "/api/admin", WAFLog.LogStatus.BLOCKED);
        highRiskLog1.setRiskScore(85);
        highRiskLog1.setAttackType("SQL_INJECTION");

        WAFLog highRiskLog2 = createTestLog("192.168.1.3", "/api/data", WAFLog.LogStatus.BLOCKED);
        highRiskLog2.setRiskScore(95);
        highRiskLog2.setAttackType("XSS");

        wafLogService.saveLog(lowRiskLog);
        wafLogService.saveLog(highRiskLog1);
        wafLogService.saveLog(highRiskLog2);

        // when
        Page<WAFLog> highRiskLogs = wafLogService.getHighRiskLogs(10);

        // then
        assertThat(highRiskLogs.getContent()).hasSize(2);
        assertThat(highRiskLogs.getContent())
            .allMatch(log -> log.getRiskScore() >= 70);
    }

    @Test
    @DisplayName("로그 통계 조회 성공")
    void getLogStatistics_Success() {
        // given
        wafLogService.saveLog(createTestLog("192.168.1.1", "/api/test1", WAFLog.LogStatus.SUCCESS));
        wafLogService.saveLog(createTestLog("192.168.1.2", "/api/test2", WAFLog.LogStatus.SUCCESS));
        wafLogService.saveLog(createTestLog("192.168.1.3", "/api/test3", WAFLog.LogStatus.BLOCKED));
        wafLogService.saveLog(createTestLog("192.168.1.4", "/api/test4", WAFLog.LogStatus.ERROR));

        // when
        WAFLogService.LogStatistics statistics = wafLogService.getLogStatistics();

        // then
        assertThat(statistics).isNotNull();
        assertThat(statistics.getTotalCount()).isEqualTo(4);
        assertThat(statistics.getSuccessCount()).isEqualTo(2);
        assertThat(statistics.getBlockedCount()).isEqualTo(1);
        assertThat(statistics.getErrorCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("기간별 로그 조회 성공")
    void getLogsByTimeRange_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime twoHoursAgo = now.minusHours(2);

        WAFLog oldLog = createTestLog("192.168.1.1", "/api/old", WAFLog.LogStatus.SUCCESS);
        oldLog.setTimestamp(twoHoursAgo);

        WAFLog recentLog = createTestLog("192.168.1.2", "/api/recent", WAFLog.LogStatus.SUCCESS);
        recentLog.setTimestamp(now.minusMinutes(30));

        wafLogService.saveLog(oldLog);
        wafLogService.saveLog(recentLog);

        // when
        Page<WAFLog> logsInRange = wafLogService.getLogsByTimeRange(oneHourAgo, now, 10);

        // then
        assertThat(logsInRange.getContent()).hasSize(1);
        assertThat(logsInRange.getContent().get(0).getRequestUri()).isEqualTo("/api/recent");
    }

    private WAFLog createTestLog(String sourceIp, String requestUri, WAFLog.LogStatus status) {
        return WAFLog.builder()
            .sourceIp(sourceIp)
            .httpMethod("GET")
            .requestUri(requestUri)
            .userAgent("Mozilla/5.0")
            .status(status)
            .responseTimeMs(150L)
            .responseStatusCode(status == WAFLog.LogStatus.SUCCESS ? 200 : 403)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
