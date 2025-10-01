package dev.waf.console.waflog.service;

import dev.waf.console.waflog.domain.WAFLog;
import dev.waf.console.waflog.repository.WAFLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * WAF 로그 서비스
 *
 * WAF 로그의 저장, 조회, 분석 기능을 제공
 * - 비동기 로그 저장으로 성능 최적화
 * - 다양한 조건의 로그 조회
 * - 로그 통계 및 분석 데이터 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WAFLogService {

    private final WAFLogRepository wafLogRepository;

    /**
     * 최근 로그 조회 (최대 500개)
     *
     * @param limit 조회할 로그 개수 (최대 500)
     * @return 최근 로그 목록
     */
    @Transactional(readOnly = true)
    public Page<WAFLog> getRecentLogs(int limit) {
        // 최대 500개로 제한
        int safeLimit = Math.min(limit, 500);
        Pageable pageable = PageRequest.of(0, safeLimit);

        log.debug("Retrieving recent {} logs", safeLimit);
        return wafLogRepository.findRecentLogs(pageable);
    }

    /**
     * 상태별 최근 로그 조회
     *
     * @param status 로그 상태
     * @param limit 조회할 로그 개수
     * @return 해당 상태의 최근 로그 목록
     */
    @Transactional(readOnly = true)
    public Page<WAFLog> getRecentLogsByStatus(WAFLog.LogStatus status, int limit) {
        int safeLimit = Math.min(limit, 500);
        Pageable pageable = PageRequest.of(0, safeLimit);

        log.debug("Retrieving recent {} logs with status: {}", safeLimit, status);
        return wafLogRepository.findByStatusOrderByTimestampDesc(status, pageable);
    }

    /**
     * 성공 로그만 조회
     *
     * @param limit 조회할 로그 개수
     * @return 성공 로그 목록
     */
    @Transactional(readOnly = true)
    public Page<WAFLog> getRecentSuccessLogs(int limit) {
        return getRecentLogsByStatus(WAFLog.LogStatus.SUCCESS, limit);
    }

    /**
     * 차단 로그만 조회
     *
     * @param limit 조회할 로그 개수
     * @return 차단 로그 목록
     */
    @Transactional(readOnly = true)
    public Page<WAFLog> getRecentBlockedLogs(int limit) {
        return getRecentLogsByStatus(WAFLog.LogStatus.BLOCKED, limit);
    }

    /**
     * 에러 로그만 조회
     *
     * @param limit 조회할 로그 개수
     * @return 에러 로그 목록
     */
    @Transactional(readOnly = true)
    public Page<WAFLog> getRecentErrorLogs(int limit) {
        return getRecentLogsByStatus(WAFLog.LogStatus.ERROR, limit);
    }

    /**
     * 특정 기간의 로그 조회
     *
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param limit 조회할 로그 개수
     * @return 해당 기간의 로그 목록
     */
    @Transactional(readOnly = true)
    public Page<WAFLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        int safeLimit = Math.min(limit, 500);
        Pageable pageable = PageRequest.of(0, safeLimit);

        log.debug("Retrieving logs from {} to {} (limit: {})", startTime, endTime, safeLimit);
        return wafLogRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime, pageable);
    }

    /**
     * 높은 위험도 로그 조회 (위험도 70 이상)
     *
     * @param limit 조회할 로그 개수
     * @return 높은 위험도 로그 목록
     */
    @Transactional(readOnly = true)
    public Page<WAFLog> getHighRiskLogs(int limit) {
        int safeLimit = Math.min(limit, 500);
        Pageable pageable = PageRequest.of(0, safeLimit);

        log.debug("Retrieving high risk logs (limit: {})", safeLimit);
        return wafLogRepository.findByRiskScoreGreaterThanEqualOrderByTimestampDesc(70, pageable);
    }

    /**
     * 로그 저장 (비동기)
     *
     * @param wafLog 저장할 로그
     * @return 저장된 로그
     */
    @Async
    @Transactional
    public CompletableFuture<WAFLog> saveLogAsync(WAFLog wafLog) {
        try {
            WAFLog savedLog = wafLogRepository.save(wafLog);
            log.debug("WAF log saved asynchronously: {} - {} - {}",
                savedLog.getStatus(), savedLog.getSourceIp(), savedLog.getRequestUri());
            return CompletableFuture.completedFuture(savedLog);
        } catch (Exception e) {
            log.error("Failed to save WAF log asynchronously", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 로그 저장 (동기)
     *
     * @param wafLog 저장할 로그
     * @return 저장된 로그
     */
    @Transactional
    public WAFLog saveLog(WAFLog wafLog) {
        WAFLog savedLog = wafLogRepository.save(wafLog);
        log.debug("WAF log saved: {} - {} - {}",
            savedLog.getStatus(), savedLog.getSourceIp(), savedLog.getRequestUri());
        return savedLog;
    }

    /**
     * 요청 처리 로그 생성 및 저장
     *
     * @param sourceIp 소스 IP
     * @param httpMethod HTTP 메서드
     * @param requestUri 요청 URI
     * @param userAgent User-Agent
     * @param status 처리 상태
     * @param responseTimeMs 응답 시간
     * @param responseStatusCode 응답 상태 코드
     * @return 저장된 로그
     */
    public CompletableFuture<WAFLog> logRequest(String sourceIp, String httpMethod, String requestUri,
                                              String userAgent, WAFLog.LogStatus status,
                                              Long responseTimeMs, Integer responseStatusCode) {
        WAFLog log = WAFLog.builder()
            .sourceIp(sourceIp)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .userAgent(userAgent)
            .status(status)
            .responseTimeMs(responseTimeMs)
            .responseStatusCode(responseStatusCode)
            .timestamp(LocalDateTime.now())
            .build();

        return saveLogAsync(log);
    }

    /**
     * 공격 탐지 로그 생성 및 저장
     *
     * @param sourceIp 소스 IP
     * @param httpMethod HTTP 메서드
     * @param requestUri 요청 URI
     * @param userAgent User-Agent
     * @param attackType 공격 유형
     * @param riskScore 위험도 점수
     * @param ruleId 매칭된 룰 ID
     * @param ruleName 매칭된 룰 이름
     * @param blockReason 차단 사유
     * @return 저장된 로그
     */
    public CompletableFuture<WAFLog> logAttackDetection(String sourceIp, String httpMethod, String requestUri,
                                                       String userAgent, String attackType, Integer riskScore,
                                                       String ruleId, String ruleName, String blockReason) {
        WAFLog log = WAFLog.builder()
            .sourceIp(sourceIp)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .userAgent(userAgent)
            .status(WAFLog.LogStatus.BLOCKED)
            .attackType(attackType)
            .riskScore(riskScore)
            .ruleId(ruleId)
            .ruleName(ruleName)
            .blockReason(blockReason)
            .timestamp(LocalDateTime.now())
            .build();

        return saveLogAsync(log);
    }

    /**
     * 로그 통계 조회
     *
     * @return 로그 통계 정보
     */
    @Transactional(readOnly = true)
    public LogStatistics getLogStatistics() {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);

        long successCount = wafLogRepository.countByStatusSince(WAFLog.LogStatus.SUCCESS, last24Hours);
        long blockedCount = wafLogRepository.countByStatusSince(WAFLog.LogStatus.BLOCKED, last24Hours);
        long errorCount = wafLogRepository.countByStatusSince(WAFLog.LogStatus.ERROR, last24Hours);
        long warningCount = wafLogRepository.countByStatusSince(WAFLog.LogStatus.WARNING, last24Hours);

        Double avgResponseTime = wafLogRepository.getAverageResponseTimeSince(lastHour);

        // 공격 유형별 차단 통계
        List<Object[]> attackTypeStats = wafLogRepository.countBlockedByAttackTypeSince(last24Hours);
        Map<String, Long> attackTypeCounts = attackTypeStats.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));

        // 상위 공격 IP
        List<Object[]> topAttackingIPs = wafLogRepository.findTopAttackingIPs(last24Hours, 10);

        return LogStatistics.builder()
            .successCount(successCount)
            .blockedCount(blockedCount)
            .errorCount(errorCount)
            .warningCount(warningCount)
            .totalCount(successCount + blockedCount + errorCount + warningCount)
            .averageResponseTimeMs(avgResponseTime != null ? avgResponseTime : 0.0)
            .attackTypeCounts(attackTypeCounts)
            .topAttackingIPs(topAttackingIPs)
            .build();
    }

    /**
     * 로그 통계 데이터 클래스
     */
    @lombok.Data
    @lombok.Builder
    public static class LogStatistics {
        private long successCount;
        private long blockedCount;
        private long errorCount;
        private long warningCount;
        private long totalCount;
        private double averageResponseTimeMs;
        private Map<String, Long> attackTypeCounts;
        private List<Object[]> topAttackingIPs;

        public double getBlockedPercentage() {
            return totalCount > 0 ? (blockedCount * 100.0) / totalCount : 0.0;
        }

        public double getSuccessPercentage() {
            return totalCount > 0 ? (successCount * 100.0) / totalCount : 0.0;
        }
    }
}