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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 대시보드 서비스 구현체
 *
 * 기존 WAFLogService, CustomRuleService를 활용하여
 * 대시보드에 필요한 데이터를 집계하고 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final WAFLogService wafLogService;
    private final CustomRuleService customRuleService;
    private final WAFLogRepository wafLogRepository;
    private final CustomRuleRepository customRuleRepository;

    // 서버 시작 시간 (uptime 계산용)
    private final Instant serverStartTime = Instant.now();

    @Override
    public WAFStatsResponse getWAFStats() {
        log.debug("Retrieving WAF statistics");

        // WAFLogService의 기존 통계 메서드 활용
        WAFLogService.LogStatistics stats = wafLogService.getLogStatistics();

        // Uptime 계산 (서버 시작 시간부터 현재까지의 시간)
        long uptime = Duration.between(serverStartTime, Instant.now()).getSeconds();

        WAFStatsResponse response = new WAFStatsResponse(
            stats.getTotalCount(),
            stats.getBlockedCount(),
            stats.getSuccessCount(),
            stats.getBlockedPercentage(),
            stats.getAverageResponseTimeMs(),
            uptime
        );

        log.debug("WAF stats retrieved: {} total requests, {} blocked, {  }% block rate",
            response.totalRequests(), response.blockedRequests(), response.blockRate());

        return response;
    }

    @Override
    public WAFStatusResponse getWAFStatus() {
        log.debug("Retrieving WAF status");

        // CustomRuleService의 기존 통계 메서드 활용
        CustomRuleService.RuleStatistics ruleStats = customRuleService.getRuleStatistics();

        // 시스템 상태 결정
        String status = determineSystemStatus();

        // 설정 마지막 업데이트 시간 조회
        String configLastUpdated = getLatestConfigUpdateTime();

        WAFStatusResponse response = new WAFStatusResponse(
            status,
            "1.0.0",  // WAF 버전 (환경 변수나 properties에서 주입 가능)
            serverStartTime.toString(),
            configLastUpdated,
            (int) ruleStats.getTotalRules(),
            (int) ruleStats.getEnabledRules()
        );

        log.debug("WAF status retrieved: status={}, rulesCount={}, activeRulesCount={}",
            response.status(), response.rulesCount(), response.activeRulesCount());

        return response;
    }

    @Override
    public List<TrafficDataResponse> getTrafficData(int hours) {
        log.debug("Retrieving traffic data for last {} hours", hours);

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);

        // Repository의 Native Query로 시간대별 집계 데이터 조회
        List<Object[]> results = wafLogRepository.getTrafficDataByHour(startTime, endTime);

        List<TrafficDataResponse> trafficData = results.stream()
            .map(row -> new TrafficDataResponse(
                (String) row[0],  // DATE_FORMAT returns String, not Timestamp
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).doubleValue()
            ))
            .collect(Collectors.toList());

        log.debug("Traffic data retrieved: {} data points", trafficData.size());

        return trafficData;
    }

    @Override
    public List<AttackEventResponse> getRecentAttacks(int limit) {
        log.debug("Retrieving recent {} attack events", limit);

        // WAFLogService의 기존 차단 로그 조회 메서드 활용
        Page<WAFLog> blockedLogs = wafLogService.getRecentBlockedLogs(limit);

        List<AttackEventResponse> attacks = blockedLogs.getContent().stream()
            .map(log -> new AttackEventResponse(
                log.getId().toString(),
                log.getTimestamp().toString(),
                log.getSourceIp() != null ? log.getSourceIp() : "0.0.0.0",
                log.getRequestUri() != null ? log.getRequestUri() : "/",
                log.getAttackType() != null ? log.getAttackType() : "UNKNOWN",
                mapSeverity(log.getRiskScore()),
                log.isBlocked(),
                log.getUserAgent() != null ? log.getUserAgent() : "",
                log.getBlockReason() != null ? log.getBlockReason() : "",
                parseMatchedRules(log.getRuleName())
            ))
            .collect(Collectors.toList());

        log.debug("Recent attacks retrieved: {} events", attacks.size());

        return attacks;
    }

    /**
     * 시스템 상태 결정
     *
     * @return 'active', 'inactive', 'error', 'maintenance'
     */
    private String determineSystemStatus() {
        try {
            // 기본적으로 서비스가 실행 중이면 active
            // 추후 확장 가능: DB 연결 상태, Kafka 연결 상태, 최근 에러 로그 개수 등 체크
            return "active";
        } catch (Exception e) {
            log.error("Error determining system status", e);
            return "error";
        }
    }

    /**
     * 가장 최근 설정 업데이트 시간 조회
     *
     * @return ISO 형식의 시간 문자열
     */
    private String getLatestConfigUpdateTime() {
        try {
            LocalDateTime latestUpdate = customRuleRepository.findLatestUpdateTime();
            if (latestUpdate != null) {
                return latestUpdate.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve latest config update time", e);
        }
        // 데이터가 없으면 서버 시작 시간 반환
        return serverStartTime.toString();
    }

    /**
     * 위험도 점수를 심각도로 매핑
     *
     * @param riskScore 위험도 점수 (0-100)
     * @return 'low', 'medium', 'high', 'critical'
     */
    private String mapSeverity(Integer riskScore) {
        if (riskScore == null) {
            return "low";
        }

        if (riskScore >= 90) {
            return "critical";
        } else if (riskScore >= 70) {
            return "high";
        } else if (riskScore >= 40) {
            return "medium";
        } else {
            return "low";
        }
    }

    /**
     * 룰 이름 문자열을 룰 목록으로 파싱
     *
     * @param ruleName 룰 이름 (쉼표로 구분된 여러 룰 가능)
     * @return 룰 목록
     */
    private List<String> parseMatchedRules(String ruleName) {
        if (ruleName == null || ruleName.isEmpty()) {
            return List.of();
        }

        // 쉼표로 구분된 여러 룰이 있을 수 있음
        if (ruleName.contains(",")) {
            return Arrays.stream(ruleName.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }

        return List.of(ruleName.trim());
    }
}
