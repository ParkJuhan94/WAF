package dev.waf.console.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Alert 엔티티
 *
 * 공격 탐지 및 보안 이벤트에 대한 알림 이력을 저장합니다.
 * - 실시간 알림 이력
 * - Slack 발송 여부
 * - 공격자 IP 추적
 * - 알림 레벨 관리
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_created_at", columnList = "createdAt"),
        @Index(name = "idx_alert_source_ip", columnList = "sourceIp"),
        @Index(name = "idx_alert_level", columnList = "level")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림 레벨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertLevel level;

    /**
     * 알림 제목
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 알림 상세 설명
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 공격자 IP (nullable - 모든 알림이 IP와 연관되는 것은 아님)
     */
    @Column(length = 45)
    private String sourceIp;

    /**
     * 영향받은 리소스 (URL, 파일 등)
     */
    @Column(length = 500)
    private String affectedResource;

    /**
     * 발생 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer occurrenceCount = 1;

    /**
     * Slack 알림 발송 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean slackSent = false;

    /**
     * 생성 시각
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Entity 생성 전 자동 설정
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (slackSent == null) {
            slackSent = false;
        }
        if (occurrenceCount == null) {
            occurrenceCount = 1;
        }
    }

    /**
     * 알림 레벨 Enum
     */
    public enum AlertLevel {
        /**
         * 낮음 - 정보성 알림
         */
        LOW,

        /**
         * 중간 - 주의 필요
         */
        MEDIUM,

        /**
         * 높음 - 긴급 대응 필요
         */
        HIGH,

        /**
         * 긴급 - 즉시 대응 필수
         */
        CRITICAL
    }
}
