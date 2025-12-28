package dev.waf.console.repository;

import dev.waf.console.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alert Repository
 *
 * 알림 데이터 접근 계층
 * - 공격자 IP 기반 조회
 * - 시간 범위 기반 집계
 * - 알림 레벨별 조회
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * 특정 IP에서 발생한 최근 알림 개수 조회
     *
     * @param ip    소스 IP
     * @param since 시작 시각
     * @return 알림 개수
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.sourceIp = :ip AND a.createdAt >= :since")
    int countBySourceIpSince(@Param("ip") String ip, @Param("since") LocalDateTime since);

    /**
     * 알림 레벨별 조회 (최신순)
     *
     * @param level 알림 레벨
     * @return 알림 목록
     */
    List<Alert> findByLevelOrderByCreatedAtDesc(Alert.AlertLevel level);

    /**
     * 특정 IP의 최근 알림 조회
     *
     * @param sourceIp 소스 IP
     * @param since    시작 시각
     * @return 알림 목록
     */
    List<Alert> findBySourceIpAndCreatedAtAfter(String sourceIp, LocalDateTime since);

    /**
     * 특정 시간 이후 발생한 CRITICAL 알림 조회
     *
     * @param since 시작 시각
     * @return 알림 목록
     */
    @Query("SELECT a FROM Alert a WHERE a.level = 'CRITICAL' AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Alert> findCriticalAlertsSince(@Param("since") LocalDateTime since);

    /**
     * Slack 미발송 CRITICAL 알림 조회
     *
     * @return 알림 목록
     */
    @Query("SELECT a FROM Alert a WHERE a.level = 'CRITICAL' AND a.slackSent = false ORDER BY a.createdAt ASC")
    List<Alert> findPendingSlackAlerts();
}
