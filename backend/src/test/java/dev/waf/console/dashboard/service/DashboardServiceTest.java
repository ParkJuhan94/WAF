package dev.waf.console.dashboard.service;

import dev.waf.console.customrule.repository.CustomRuleRepository;
import dev.waf.console.customrule.service.CustomRuleService;
import dev.waf.console.dashboard.api.dto.AttackEventResponse;
import dev.waf.console.dashboard.api.dto.TrafficDataResponse;
import dev.waf.console.dashboard.api.dto.WAFStatsResponse;
import dev.waf.console.dashboard.api.dto.WAFStatusResponse;
import dev.waf.console.waflog.domain.WAFLog;
import dev.waf.console.waflog.repository.WAFLogRepository;
import dev.waf.console.waflog.service.WAFLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * DashboardService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService 테스트")
class DashboardServiceTest {

    @Mock
    private WAFLogService wafLogService;

    @Mock
    private CustomRuleService customRuleService;

    @Mock
    private WAFLogRepository wafLogRepository;

    @Mock
    private CustomRuleRepository customRuleRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private WAFLogService.LogStatistics mockLogStats;
    private CustomRuleService.RuleStatistics mockRuleStats;

    @BeforeEach
    void setUp() {
        // Mock 로그 통계 데이터
        Map<String, Long> attackTypeCounts = new HashMap<>();
        attackTypeCounts.put("SQL_INJECTION", 10L);
        attackTypeCounts.put("XSS", 5L);

        List<Object[]> topIPs = new ArrayList<>();
        topIPs.add(new Object[]{"192.168.1.100", 15L});

        mockLogStats = WAFLogService.LogStatistics.builder()
            .successCount(850L)
            .blockedCount(100L)
            .errorCount(30L)
            .warningCount(20L)
            .totalCount(1000L)
            .averageResponseTimeMs(45.5)
            .attackTypeCounts(attackTypeCounts)
            .topAttackingIPs(topIPs)
            .build();

        // Mock 룰 통계 데이터
        mockRuleStats = CustomRuleService.RuleStatistics.builder()
            .totalRules(50L)
            .enabledRules(40L)
            .disabledRules(10L)
            .totalMatches(500L)
            .totalBlocks(100L)
            .build();
    }

    @Test
    @DisplayName("WAF 통계 조회 - 정상 케이스")
    void getWAFStats_shouldReturnCorrectStatistics() {
        // Given
        when(wafLogService.getLogStatistics()).thenReturn(mockLogStats);

        // When
        WAFStatsResponse response = dashboardService.getWAFStats();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.totalRequests()).isEqualTo(1000L);
        assertThat(response.blockedRequests()).isEqualTo(100L);
        assertThat(response.allowedRequests()).isEqualTo(850L);
        assertThat(response.blockRate()).isEqualTo(10.0);
        assertThat(response.avgResponseTime()).isEqualTo(45.5);
        assertThat(response.uptime()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("WAF 상태 조회 - 정상 케이스")
    void getWAFStatus_shouldReturnSystemStatus() {
        // Given
        when(customRuleService.getRuleStatistics()).thenReturn(mockRuleStats);
        when(customRuleRepository.findLatestUpdateTime()).thenReturn(LocalDateTime.now());

        // When
        WAFStatusResponse response = dashboardService.getWAFStatus();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("active");
        assertThat(response.version()).isEqualTo("1.0.0");
        assertThat(response.rulesCount()).isEqualTo(50);
        assertThat(response.activeRulesCount()).isEqualTo(40);
        assertThat(response.lastRestart()).isNotNull();
        assertThat(response.configLastUpdated()).isNotNull();
    }

    @Test
    @DisplayName("트래픽 데이터 조회 - 시간대별 집계")
    void getTrafficData_shouldReturnHourlyData() {
        // Given
        List<Object[]> mockTrafficData = new ArrayList<>();
        Timestamp timestamp = Timestamp.valueOf("2024-12-10 14:00:00");
        mockTrafficData.add(new Object[]{timestamp, 234L, 12L, 222L, 42.5});
        mockTrafficData.add(new Object[]{timestamp, 198L, 8L, 190L, 38.2});

        when(wafLogRepository.getTrafficDataByHour(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(mockTrafficData);

        // When
        List<TrafficDataResponse> response = dashboardService.getTrafficData(24);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        TrafficDataResponse first = response.get(0);
        assertThat(first.totalRequests()).isEqualTo(234L);
        assertThat(first.blockedRequests()).isEqualTo(12L);
        assertThat(first.allowedRequests()).isEqualTo(222L);
        assertThat(first.responseTime()).isEqualTo(42.5);
    }

    @Test
    @DisplayName("최근 공격 이벤트 조회 - 차단된 로그만")
    void getRecentAttacks_shouldReturnBlockedLogs() {
        // Given
        List<WAFLog> mockLogs = new ArrayList<>();
        WAFLog log1 = WAFLog.builder()
            .id(1L)
            .timestamp(LocalDateTime.now())
            .sourceIp("192.168.1.100")
            .requestUri("/api/users")
            .attackType("SQL_INJECTION")
            .riskScore(85)
            .status(WAFLog.LogStatus.BLOCKED)
            .userAgent("Mozilla/5.0")
            .blockReason("SQL Injection detected")
            .ruleName("SQL-001")
            .build();

        WAFLog log2 = WAFLog.builder()
            .id(2L)
            .timestamp(LocalDateTime.now())
            .sourceIp("192.168.1.101")
            .requestUri("/api/login")
            .attackType("XSS")
            .riskScore(65)
            .status(WAFLog.LogStatus.BLOCKED)
            .userAgent("Mozilla/5.0")
            .blockReason("XSS detected")
            .ruleName("XSS-002")
            .build();

        mockLogs.add(log1);
        mockLogs.add(log2);

        Page<WAFLog> mockPage = new PageImpl<>(mockLogs);
        when(wafLogService.getRecentBlockedLogs(anyInt())).thenReturn(mockPage);

        // When
        List<AttackEventResponse> response = dashboardService.getRecentAttacks(10);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(2);

        AttackEventResponse first = response.get(0);
        assertThat(first.id()).isEqualTo("1");
        assertThat(first.sourceIp()).isEqualTo("192.168.1.100");
        assertThat(first.targetPath()).isEqualTo("/api/users");
        assertThat(first.attackType()).isEqualTo("SQL_INJECTION");
        assertThat(first.severity()).isEqualTo("high"); // riskScore 85 -> high
        assertThat(first.blocked()).isTrue();
        assertThat(first.matchedRules()).contains("SQL-001");
    }

    @Test
    @DisplayName("심각도 매핑 - 위험도 점수에 따른 매핑")
    void testSeverityMapping() {
        // Given: 여러 위험도 점수를 가진 로그 생성
        WAFLog criticalLog = createMockLog(95);
        WAFLog highLog = createMockLog(75);
        WAFLog mediumLog = createMockLog(50);
        WAFLog lowLog = createMockLog(20);

        List<WAFLog> logs = List.of(criticalLog, highLog, mediumLog, lowLog);
        Page<WAFLog> mockPage = new PageImpl<>(logs);
        when(wafLogService.getRecentBlockedLogs(anyInt())).thenReturn(mockPage);

        // When
        List<AttackEventResponse> response = dashboardService.getRecentAttacks(10);

        // Then
        assertThat(response.get(0).severity()).isEqualTo("critical");
        assertThat(response.get(1).severity()).isEqualTo("high");
        assertThat(response.get(2).severity()).isEqualTo("medium");
        assertThat(response.get(3).severity()).isEqualTo("low");
    }

    private WAFLog createMockLog(int riskScore) {
        return WAFLog.builder()
            .id(1L)
            .timestamp(LocalDateTime.now())
            .sourceIp("192.168.1.100")
            .requestUri("/test")
            .attackType("TEST")
            .riskScore(riskScore)
            .status(WAFLog.LogStatus.BLOCKED)
            .userAgent("Test")
            .blockReason("Test")
            .ruleName("TEST-001")
            .build();
    }
}
