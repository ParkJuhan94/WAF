package dev.waf.console.service;

import dev.waf.console.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * WAF 이벤트 발행 서비스
 *
 * 모든 WAF 이벤트를 Kafka로 발행하는 중앙화된 서비스
 * 이벤트 기반 아키텍처의 핵심 컴포넌트
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.attacks:waf.attacks}")
    private String attacksTopic;

    @Value("${app.kafka.topics.logs:waf.logs}")
    private String logsTopic;

    @Value("${app.kafka.topics.alerts:waf.alerts}")
    private String alertsTopic;

    @Value("${app.kafka.topics.metrics:waf.metrics}")
    private String metricsTopic;

    @Value("${app.kafka.topics.audit:waf.audit}")
    private String auditTopic;

    @Value("${spring.application.name:waf-console}")
    private String applicationName;

    /**
     * 공격 탐지 이벤트 발행
     */
    public CompletableFuture<SendResult<String, Object>> publishAttackDetected(AttackDetectedEvent event) {
        enrichEvent(event);
        log.info("Publishing attack detected event: attackType={}, sourceIp={}, riskScore={}",
            event.getAttackType(), event.getSourceIp(), event.getRiskScore());

        return kafkaTemplate.send(attacksTopic, event.getSourceIp(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Attack event sent successfully: partition={}, offset={}",
                        result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send attack event", ex);
                }
            });
    }

    /**
     * 접근 로그 이벤트 발행
     */
    public CompletableFuture<SendResult<String, Object>> publishAccessLog(AccessLogEvent event) {
        enrichEvent(event);
        log.debug("Publishing access log event: ip={}, uri={}, status={}",
            event.getClientIp(), event.getUri(), event.getStatusCode());

        return kafkaTemplate.send(logsTopic, event.getClientIp(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("Failed to send access log event", ex);
                }
            });
    }

    /**
     * 보안 알림 이벤트 발행
     */
    public CompletableFuture<SendResult<String, Object>> publishSecurityAlert(SecurityAlertEvent event) {
        enrichEvent(event);
        log.warn("Publishing security alert: level={}, title={}, sourceIp={}",
            event.getLevel(), event.getTitle(), event.getSourceIp());

        return kafkaTemplate.send(alertsTopic, event.getSourceIp(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Security alert sent successfully");
                } else {
                    log.error("Failed to send security alert", ex);
                }
            });
    }

    /**
     * 메트릭 이벤트 발행
     */
    public CompletableFuture<SendResult<String, Object>> publishMetrics(MetricsEvent event) {
        enrichEvent(event);
        log.debug("Publishing metrics event: name={}, value={}",
            event.getMetricName(), event.getValue());

        return kafkaTemplate.send(metricsTopic, event.getMetricName(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("Failed to send metrics event", ex);
                }
            });
    }

    /**
     * 감사 로그 이벤트 발행
     */
    public CompletableFuture<SendResult<String, Object>> publishAudit(AuditEvent event) {
        enrichEvent(event);
        log.info("Publishing audit event: action={}, user={}, resource={}",
            event.getAction(), event.getUsername(), event.getResource());

        return kafkaTemplate.send(auditTopic, event.getUserId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Audit event sent successfully");
                } else {
                    log.error("Failed to send audit event", ex);
                }
            });
    }

    /**
     * 이벤트 공통 속성 설정
     */
    private void enrichEvent(WAFEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        if (event.getSource() == null) {
            event.setSource(applicationName);
        }
        if (event.getCorrelationId() == null) {
            // 현재 요청의 상관관계 ID가 있다면 설정 (향후 트레이싱 연동)
            event.setCorrelationId(generateCorrelationId());
        }
    }

    /**
     * 상관관계 ID 생성
     */
    private String generateCorrelationId() {
        // 현재는 간단한 UUID 사용, 향후 분산 트레이싱과 연동
        return UUID.randomUUID().toString();
    }

    /**
     * 배치 이벤트 발행 (성능 최적화)
     */
    public void publishBatchEvents(java.util.List<WAFEvent> events) {
        events.forEach(event -> {
            switch (event.getEventType()) {
                case ATTACK_DETECTED -> publishAttackDetected((AttackDetectedEvent) event);
                case ACCESS_LOG -> publishAccessLog((AccessLogEvent) event);
                case SECURITY_ALERT -> publishSecurityAlert((SecurityAlertEvent) event);
                case METRICS -> publishMetrics((MetricsEvent) event);
                case AUDIT -> publishAudit((AuditEvent) event);
            }
        });
        log.info("Published {} events in batch", events.size());
    }
}