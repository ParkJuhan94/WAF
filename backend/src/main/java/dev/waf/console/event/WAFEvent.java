package dev.waf.console.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WAF 이벤트 베이스 클래스
 *
 * 모든 WAF 관련 이벤트의 공통 속성을 정의하고
 * JSON 직렬화를 위한 타입 정보를 포함
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AttackDetectedEvent.class, name = "ATTACK_DETECTED"),
    @JsonSubTypes.Type(value = AccessLogEvent.class, name = "ACCESS_LOG"),
    @JsonSubTypes.Type(value = SecurityAlertEvent.class, name = "SECURITY_ALERT"),
    @JsonSubTypes.Type(value = MetricsEvent.class, name = "METRICS"),
    @JsonSubTypes.Type(value = AuditEvent.class, name = "AUDIT")
})
public abstract class WAFEvent {

    /**
     * 이벤트 고유 ID
     */
    private String eventId;

    /**
     * 이벤트 발생 시간
     */
    private LocalDateTime timestamp;

    /**
     * 이벤트 소스 (WAF 인스턴스 ID)
     */
    private String source;

    /**
     * 이벤트 버전 (스키마 진화 지원)
     */
    private String version = "1.0";

    /**
     * 이벤트 상관관계 ID (추적용)
     */
    private String correlationId;

    /**
     * 이벤트 메타데이터
     */
    private java.util.Map<String, Object> metadata;

    /**
     * 이벤트 타입 반환
     */
    public abstract WAFEventType getEventType();

    /**
     * WAF 이벤트 타입 열거형
     */
    public enum WAFEventType {
        ATTACK_DETECTED,    // 공격 탐지
        ACCESS_LOG,         // 접근 로그
        SECURITY_ALERT,     // 보안 알림
        METRICS,           // 성능 메트릭
        AUDIT              // 감사 로그
    }
}