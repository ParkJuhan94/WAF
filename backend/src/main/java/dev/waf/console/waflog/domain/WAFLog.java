package dev.waf.console.waflog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * WAF 로그 엔티티
 *
 * WAF의 모든 요청 처리 결과를 기록하는 로그 테이블
 * - 성공/실패한 요청들의 상세 정보 저장
 * - 보안 분석 및 모니터링을 위한 데이터 제공
 * - 실시간 대시보드 및 리포트 생성 지원
 */
@Entity
@Table(name = "waf_logs", indexes = {
    @Index(name = "idx_waf_logs_timestamp", columnList = "timestamp"),
    @Index(name = "idx_waf_logs_status", columnList = "status"),
    @Index(name = "idx_waf_logs_source_ip", columnList = "sourceIp"),
    @Index(name = "idx_waf_logs_attack_type", columnList = "attackType")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WAFLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 로그 발생 시간
     */
    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    /**
     * 요청 처리 상태 (SUCCESS, BLOCKED, ERROR)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LogStatus status;

    /**
     * 클라이언트 IP 주소
     */
    @Column(name = "source_ip", nullable = false, length = 45)
    private String sourceIp;

    /**
     * HTTP 메서드 (GET, POST, PUT, DELETE 등)
     */
    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    /**
     * 요청 URI
     */
    @Column(name = "request_uri", nullable = false, length = 1000)
    private String requestUri;

    /**
     * User-Agent 정보
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 탐지된 공격 유형 (SQL_INJECTION, XSS, CSRF 등)
     */
    @Column(name = "attack_type", length = 50)
    private String attackType;

    /**
     * 위험도 점수 (0-100)
     */
    @Column(name = "risk_score")
    private Integer riskScore;

    /**
     * 매칭된 WAF 룰 ID
     */
    @Column(name = "rule_id", length = 100)
    private String ruleId;

    /**
     * 매칭된 WAF 룰 이름
     */
    @Column(name = "rule_name", length = 200)
    private String ruleName;

    /**
     * 차단 사유 (BLOCKED인 경우)
     */
    @Column(name = "block_reason", length = 500)
    private String blockReason;

    /**
     * 응답 시간 (밀리초)
     */
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    /**
     * 응답 상태 코드
     */
    @Column(name = "response_status_code")
    private Integer responseStatusCode;

    /**
     * 요청 페이로드 크기 (바이트)
     */
    @Column(name = "payload_size")
    private Long payloadSize;

    /**
     * 지리적 위치 정보 (국가 코드)
     */
    @Column(name = "geo_country", length = 2)
    private String geoCountry;

    /**
     * 추가 메타데이터 (JSON 형태)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * 세션 ID (있는 경우)
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * 로그 처리 상태
     */
    public enum LogStatus {
        /**
         * 요청이 성공적으로 처리됨
         */
        SUCCESS,

        /**
         * 요청이 WAF에 의해 차단됨
         */
        BLOCKED,

        /**
         * 처리 중 오류 발생
         */
        ERROR,

        /**
         * 경고 수준의 의심스러운 요청
         */
        WARNING
    }

    /**
     * 정적 팩토리 메서드 - 성공 로그 생성
     */
    public static WAFLog createSuccessLog(String sourceIp, String httpMethod, String requestUri,
                                         String userAgent, Long responseTimeMs, Integer responseStatusCode) {
        return WAFLog.builder()
            .sourceIp(sourceIp)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .userAgent(userAgent)
            .status(LogStatus.SUCCESS)
            .responseTimeMs(responseTimeMs)
            .responseStatusCode(responseStatusCode)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * 정적 팩토리 메서드 - 차단 로그 생성
     */
    public static WAFLog createBlockedLog(String sourceIp, String httpMethod, String requestUri,
                                         String userAgent, String attackType, Integer riskScore,
                                         String ruleId, String ruleName, String blockReason) {
        return WAFLog.builder()
            .sourceIp(sourceIp)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .userAgent(userAgent)
            .status(LogStatus.BLOCKED)
            .attackType(attackType)
            .riskScore(riskScore)
            .ruleId(ruleId)
            .ruleName(ruleName)
            .blockReason(blockReason)
            .responseStatusCode(403)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * 정적 팩토리 메서드 - 에러 로그 생성
     */
    public static WAFLog createErrorLog(String sourceIp, String httpMethod, String requestUri,
                                       String userAgent, String blockReason) {
        return WAFLog.builder()
            .sourceIp(sourceIp)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .userAgent(userAgent)
            .status(LogStatus.ERROR)
            .blockReason(blockReason)
            .responseStatusCode(500)
            .timestamp(LocalDateTime.now())
            .build();
    }

    /**
     * 로그가 차단된 요청인지 확인
     */
    public boolean isBlocked() {
        return LogStatus.BLOCKED.equals(this.status);
    }

    /**
     * 로그가 성공한 요청인지 확인
     */
    public boolean isSuccess() {
        return LogStatus.SUCCESS.equals(this.status);
    }

    /**
     * 위험도가 높은 로그인지 확인 (위험도 70 이상)
     */
    public boolean isHighRisk() {
        return riskScore != null && riskScore >= 70;
    }
}