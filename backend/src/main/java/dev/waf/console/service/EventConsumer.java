package dev.waf.console.service;

import dev.waf.console.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * WAF 이벤트 소비 서비스
 *
 * Kafka에서 받은 이벤트를 실시간으로 처리
 * - 공격 탐지시 즉시 대응
 * - 로그 데이터 실시간 분석
 * - 알림 및 메트릭 처리
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final AlertService alertService;
    private final MetricsService metricsService;
    private final AuditService auditService;

    /**
     * 공격 탐지 이벤트 처리
     * 고위험 공격에 대한 즉시 대응
     */
    @KafkaListener(topics = "${app.kafka.topics.attacks:waf.attacks}",
                   groupId = "${spring.kafka.consumer.group-id:waf-console-group}")
    public void handleAttackDetected(@Payload AttackDetectedEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {
        try {
            log.info("Processing attack event: type={}, sourceIp={}, riskScore={} [{}:{}:{}]",
                event.getAttackType(), event.getSourceIp(), event.getRiskScore(),
                topic, partition, offset);

            // 1. 고위험 공격 즉시 알림
            if (event.getRiskScore() != null && event.getRiskScore() >= 80) {
                alertService.sendCriticalAlert(event);
            }

            // 2. IP 기반 자동 차단 검토
            if (shouldAutoBlock(event)) {
                alertService.requestAutoBlock(event.getSourceIp(), event.getAttackType());
            }

            // 3. 공격 패턴 분석 및 룰 업데이트 제안
            analyzeAttackPattern(event);

            // 4. 실시간 대시보드 업데이트
            metricsService.updateAttackMetrics(event);

            // 5. 감사 로그 기록
            auditService.logSecurityEvent(event);

            acknowledgment.acknowledge();
            log.debug("Attack event processed successfully");

        } catch (Exception e) {
            log.error("Failed to process attack event: {}", event.getEventId(), e);
            // 에러 발생시 DLQ로 이동하거나 재시도 처리는 Kafka 설정에서 관리
        }
    }

    /**
     * 접근 로그 이벤트 처리
     * 트래픽 패턴 분석 및 성능 모니터링
     */
    @KafkaListener(topics = "${app.kafka.topics.logs:waf.logs}",
                   groupId = "${spring.kafka.consumer.group-id:waf-console-group}")
    public void handleAccessLog(@Payload AccessLogEvent event,
                              Acknowledgment acknowledgment) {
        try {
            log.debug("Processing access log: ip={}, uri={}, status={}",
                event.getClientIp(), event.getUri(), event.getStatusCode());

            // 1. 트래픽 통계 업데이트
            metricsService.updateTrafficMetrics(event);

            // 2. 이상 패턴 탐지 (예: 비정상적인 요청 빈도)
            detectAnomalousPattern(event);

            // 3. 성능 메트릭 수집
            if (event.getResponseTime() != null && event.getResponseTime() > 5000) {
                metricsService.recordSlowResponse(event);
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.warn("Failed to process access log: {}", event.getEventId(), e);
        }
    }

    /**
     * 보안 알림 이벤트 처리
     * 알림 라우팅 및 에스컬레이션
     */
    @KafkaListener(topics = "${app.kafka.topics.alerts:waf.alerts}",
                   groupId = "${spring.kafka.consumer.group-id:waf-console-group}")
    public void handleSecurityAlert(@Payload SecurityAlertEvent event,
                                  Acknowledgment acknowledgment) {
        try {
            log.warn("Processing security alert: level={}, title={}",
                event.getLevel(), event.getTitle());

            // 1. 알림 레벨별 라우팅
            switch (event.getLevel()) {
                case CRITICAL -> alertService.sendImmediateNotification(event);
                case HIGH -> alertService.sendUrgentNotification(event);
                case MEDIUM -> alertService.sendStandardNotification(event);
                case LOW -> alertService.logAlert(event);
            }

            // 2. 알림 히스토리 저장
            alertService.saveAlertHistory(event);

            // 3. 자동 대응 규칙 실행
            executeAutomatedResponse(event);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process security alert: {}", event.getEventId(), e);
        }
    }

    /**
     * 메트릭 이벤트 처리
     * 실시간 성능 지표 수집 및 분석
     */
    @KafkaListener(topics = "${app.kafka.topics.metrics:waf.metrics}",
                   groupId = "${spring.kafka.consumer.group-id:waf-console-group}")
    public void handleMetrics(@Payload MetricsEvent event,
                            Acknowledgment acknowledgment) {
        try {
            log.debug("Processing metrics: name={}, value={}",
                event.getMetricName(), event.getValue());

            // 1. 메트릭 저장 및 집계
            metricsService.recordMetric(event);

            // 2. 임계값 체크 및 알림
            metricsService.checkThresholds(event);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.warn("Failed to process metrics: {}", event.getEventId(), e);
        }
    }

    /**
     * 감사 로그 이벤트 처리
     * 규정 준수 및 보안 감사를 위한 로그 처리
     */
    @KafkaListener(topics = "${app.kafka.topics.audit:waf.audit}",
                   groupId = "${spring.kafka.consumer.group-id:waf-console-group}")
    public void handleAudit(@Payload AuditEvent event,
                          Acknowledgment acknowledgment) {
        try {
            log.info("Processing audit event: action={}, user={}, resource={}",
                event.getAction(), event.getUsername(), event.getResource());

            // 1. 감사 로그 저장 (장기 보존)
            auditService.saveAuditLog(event);

            // 2. 의심스러운 활동 탐지
            if (auditService.isSuspiciousActivity(event)) {
                alertService.createSuspiciousActivityAlert(event);
            }

            // 3. 규정 준수 체크
            auditService.checkComplianceRules(event);

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process audit event: {}", event.getEventId(), e);
        }
    }

    /**
     * 자동 차단 여부 결정
     */
    private boolean shouldAutoBlock(AttackDetectedEvent event) {
        // 높은 위험도 + 특정 공격 타입 + 반복 공격 패턴
        return event.getRiskScore() != null &&
               event.getRiskScore() >= 90 &&
               isCriticalAttackType(event.getAttackType()) &&
               alertService.isRepeatedAttacker(event.getSourceIp());
    }

    /**
     * 중요 공격 타입 체크
     */
    private boolean isCriticalAttackType(AttackDetectedEvent.AttackType attackType) {
        return attackType == AttackDetectedEvent.AttackType.SQL_INJECTION ||
               attackType == AttackDetectedEvent.AttackType.COMMAND_INJECTION ||
               attackType == AttackDetectedEvent.AttackType.DESERIALIZATION;
    }

    /**
     * 공격 패턴 분석
     */
    private void analyzeAttackPattern(AttackDetectedEvent event) {
        // 공격 패턴 분석 로직
        // - 시그니처 패턴 학습
        // - 새로운 변종 탐지
        // - WAF 룰 업데이트 제안
        log.debug("Analyzing attack pattern for signature: {}", event.getSignature());
    }

    /**
     * 이상 패턴 탐지
     */
    private void detectAnomalousPattern(AccessLogEvent event) {
        // 이상 패턴 탐지 로직
        // - 비정상적인 요청 빈도
        // - 의심스러운 URL 패턴
        // - 봇 트래픽 식별
        log.debug("Detecting anomalous pattern for IP: {}", event.getClientIp());
    }

    /**
     * 자동 대응 규칙 실행
     */
    private void executeAutomatedResponse(SecurityAlertEvent event) {
        // 자동 대응 규칙 실행
        // - 자동 차단
        // - 트래픽 제한
        // - 추가 모니터링 활성화
        log.debug("Executing automated response for alert: {}", event.getTitle());
    }
}