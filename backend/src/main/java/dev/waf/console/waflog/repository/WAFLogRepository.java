package dev.waf.console.waflog.repository;

import dev.waf.console.waflog.domain.WAFLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WAF 로그 Repository
 *
 * WAF 로그 데이터에 대한 데이터베이스 접근을 담당
 * - 최근 로그 조회
 * - 상태별 로그 필터링
 * - 통계 데이터 조회
 */
@Repository
public interface WAFLogRepository extends JpaRepository<WAFLog, Long> {

    /**
     * 최근 로그를 timestamp 기준으로 내림차순 정렬하여 조회
     *
     * @param pageable 페이징 정보 (최대 500개)
     * @return 최근 로그 목록
     */
    @Query("SELECT w FROM WAFLog w ORDER BY w.timestamp DESC")
    Page<WAFLog> findRecentLogs(Pageable pageable);

    /**
     * 특정 상태의 최근 로그 조회
     *
     * @param status 로그 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 최근 로그 목록
     */
    Page<WAFLog> findByStatusOrderByTimestampDesc(WAFLog.LogStatus status, Pageable pageable);

    /**
     * 성공 로그만 최근 순으로 조회
     *
     * @param pageable 페이징 정보
     * @return 성공 로그 목록
     */
    default Page<WAFLog> findRecentSuccessLogs(Pageable pageable) {
        return findByStatusOrderByTimestampDesc(WAFLog.LogStatus.SUCCESS, pageable);
    }

    /**
     * 차단 로그만 최근 순으로 조회
     *
     * @param pageable 페이징 정보
     * @return 차단 로그 목록
     */
    default Page<WAFLog> findRecentBlockedLogs(Pageable pageable) {
        return findByStatusOrderByTimestampDesc(WAFLog.LogStatus.BLOCKED, pageable);
    }

    /**
     * 에러 로그만 최근 순으로 조회
     *
     * @param pageable 페이징 정보
     * @return 에러 로그 목록
     */
    default Page<WAFLog> findRecentErrorLogs(Pageable pageable) {
        return findByStatusOrderByTimestampDesc(WAFLog.LogStatus.ERROR, pageable);
    }

    /**
     * 특정 기간 내의 로그 조회
     *
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param pageable 페이징 정보
     * @return 해당 기간의 로그 목록
     */
    Page<WAFLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Pageable pageable
    );

    /**
     * 특정 IP의 로그 조회
     *
     * @param sourceIp 소스 IP
     * @param pageable 페이징 정보
     * @return 해당 IP의 로그 목록
     */
    Page<WAFLog> findBySourceIpOrderByTimestampDesc(String sourceIp, Pageable pageable);

    /**
     * 특정 공격 유형의 로그 조회
     *
     * @param attackType 공격 유형
     * @param pageable 페이징 정보
     * @return 해당 공격 유형의 로그 목록
     */
    Page<WAFLog> findByAttackTypeOrderByTimestampDesc(String attackType, Pageable pageable);

    /**
     * 높은 위험도 로그 조회 (위험도 70 이상)
     *
     * @param minRiskScore 최소 위험도
     * @param pageable 페이징 정보
     * @return 높은 위험도 로그 목록
     */
    Page<WAFLog> findByRiskScoreGreaterThanEqualOrderByTimestampDesc(
        Integer minRiskScore,
        Pageable pageable
    );

    /**
     * 최근 24시간 내 상태별 로그 개수 조회
     *
     * @param status 로그 상태
     * @param since 기준 시간 (24시간 전)
     * @return 로그 개수
     */
    @Query("SELECT COUNT(w) FROM WAFLog w WHERE w.status = :status AND w.timestamp >= :since")
    long countByStatusSince(@Param("status") WAFLog.LogStatus status, @Param("since") LocalDateTime since);

    /**
     * 최근 24시간 내 공격 유형별 차단 로그 개수 조회
     *
     * @param since 기준 시간
     * @return 공격 유형별 차단 개수
     */
    @Query("SELECT w.attackType, COUNT(w) FROM WAFLog w " +
           "WHERE w.status = 'BLOCKED' AND w.timestamp >= :since " +
           "GROUP BY w.attackType ORDER BY COUNT(w) DESC")
    List<Object[]> countBlockedByAttackTypeSince(@Param("since") LocalDateTime since);

    /**
     * 최근 24시간 내 상위 공격 IP 조회
     *
     * @param since 기준 시간
     * @param limit 조회할 IP 개수
     * @return IP별 공격 시도 횟수
     */
    @Query(value = "SELECT source_ip, COUNT(*) as attack_count " +
                   "FROM waf_logs " +
                   "WHERE status = 'BLOCKED' AND timestamp >= :since " +
                   "GROUP BY source_ip " +
                   "ORDER BY attack_count DESC " +
                   "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopAttackingIPs(@Param("since") LocalDateTime since, @Param("limit") int limit);

    /**
     * 평균 응답 시간 조회 (최근 1시간)
     *
     * @param since 기준 시간
     * @return 평균 응답 시간 (밀리초)
     */
    @Query("SELECT AVG(w.responseTimeMs) FROM WAFLog w WHERE w.timestamp >= :since")
    Double getAverageResponseTimeSince(@Param("since") LocalDateTime since);

    /**
     * 특정 룰로 차단된 로그 개수 조회
     *
     * @param ruleId 룰 ID
     * @param since 기준 시간
     * @return 차단 개수
     */
    @Query("SELECT COUNT(w) FROM WAFLog w WHERE w.ruleId = :ruleId AND w.status = 'BLOCKED' AND w.timestamp >= :since")
    long countBlockedByRuleSince(@Param("ruleId") String ruleId, @Param("since") LocalDateTime since);

    /**
     * 시간대별 트래픽 데이터 집계
     * 1시간 단위로 그룹화하여 통계 반환
     *
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 시간대별 집계 데이터 [hour, total, blocked, allowed, avg_response_time]
     */
    @Query(value = """
        SELECT
            DATE_FORMAT(timestamp, '%Y-%m-%d %H:00:00') as hour,
            COUNT(*) as total,
            SUM(CASE WHEN status = 'BLOCKED' THEN 1 ELSE 0 END) as blocked,
            SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as allowed,
            COALESCE(AVG(response_time_ms), 0.0) as avg_response_time
        FROM waf_logs
        WHERE timestamp >= :startTime AND timestamp < :endTime
        GROUP BY DATE_FORMAT(timestamp, '%Y-%m-%d %H:00:00')
        ORDER BY hour
        """, nativeQuery = true)
    List<Object[]> getTrafficDataByHour(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}