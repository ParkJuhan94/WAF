package dev.waf.console.service;

import dev.waf.console.domain.Alert;
import dev.waf.console.event.AttackDetectedEvent;
import dev.waf.console.event.AuditEvent;
import dev.waf.console.event.SecurityAlertEvent;
import dev.waf.console.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 서비스 구현체
 *
 * 공격 탐지 및 보안 이벤트에 대한 알림 처리
 * - Slack 알림 발송
 * - WebSocket 실시간 브로드캐스트
 * - Alert 이력 데이터베이스 저장
 * - 반복 공격자 탐지
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final SlackNotificationService slackNotificationService;
    private final WebSocketBroadcastService webSocketBroadcastService;

    /**
     * 긴급 공격 알림 전송
     * 1. Alert 저장
     * 2. Slack 알림
     * 3. WebSocket 브로드캐스트
     */
    @Override
    public void sendCriticalAlert(AttackDetectedEvent event) {
        log.warn("CRITICAL ALERT: Attack from {}, Type: {}",
                event.getSourceIp(), event.getAttackType());

        try {
            // 1. Alert 엔티티 생성 및 저장
            Alert alert = Alert.builder()
                    .level(Alert.AlertLevel.CRITICAL)
                    .title("Critical Attack: " + event.getAttackType())
                    .description(buildDescription(event))
                    .sourceIp(event.getSourceIp())
                    .affectedResource(event.getTargetUrl())
                    .occurrenceCount(1)
                    .slackSent(false)
                    .build();

            alertRepository.save(alert);

            // 2. Slack 알림 발송
            slackNotificationService.sendCriticalAlert(event);

            // Slack 발송 여부 업데이트
            alert.setSlackSent(true);
            alertRepository.save(alert);

            // 3. WebSocket 브로드캐스트 (이미 EventConsumer에서 수행)
            // webSocketBroadcastService.broadcastAttackEvent(event);

            log.info("Critical alert processed: eventId={}, sourceIp={}",
                    event.getEventId(), event.getSourceIp());

        } catch (Exception e) {
            log.error("Failed to send critical alert: {}", event.getEventId(), e);
        }
    }

    /**
     * 자동 차단 요청
     * TODO: 실제 차단 로직은 Phase 2에서 구현 (IP 블랙리스트 등)
     */
    @Override
    public void requestAutoBlock(String sourceIp, AttackDetectedEvent.AttackType attackType) {
        log.warn("AUTO BLOCK REQUEST: IP {} for attack type {}", sourceIp, attackType);

        try {
            // Alert 저장 (자동 차단 요청 이력)
            Alert alert = Alert.builder()
                    .level(Alert.AlertLevel.CRITICAL)
                    .title("Auto Block Request: " + attackType)
                    .description(String.format("Automatic block requested for IP %s due to %s attack",
                            sourceIp, attackType))
                    .sourceIp(sourceIp)
                    .occurrenceCount(1)
                    .build();

            alertRepository.save(alert);

            // TODO: 실제 차단 로직
            // - IP를 blacklist 테이블에 추가
            // - Redis 캐시에 차단 목록 업데이트
            // - WAF 규칙 동적 업데이트

            log.info("Auto block request logged for IP: {}", sourceIp);

        } catch (Exception e) {
            log.error("Failed to process auto block request for IP: {}", sourceIp, e);
        }
    }

    /**
     * 반복 공격자 체크
     * 최근 10분 동안 3회 이상 공격 시도 시 반복 공격자로 판단
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isRepeatedAttacker(String sourceIp) {
        try {
            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
            int attackCount = alertRepository.countBySourceIpSince(sourceIp, tenMinutesAgo);

            boolean isRepeated = attackCount >= 3;

            if (isRepeated) {
                log.warn("Repeated attacker detected: {} ({} attacks in 10 min)",
                        sourceIp, attackCount);
            }

            return isRepeated;

        } catch (Exception e) {
            log.error("Failed to check repeated attacker: {}", sourceIp, e);
            return false; // Fail safe
        }
    }

    /**
     * 즉시 알림 전송 (CRITICAL 레벨)
     */
    @Override
    public void sendImmediateNotification(SecurityAlertEvent event) {
        log.error("IMMEDIATE NOTIFICATION: {}", event.getDescription());

        try {
            Alert alert = Alert.builder()
                    .level(Alert.AlertLevel.CRITICAL)
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .sourceIp(event.getSourceIp())
                    .affectedResource(event.getAffectedResource())
                    .occurrenceCount(event.getOccurrenceCount())
                    .build();

            alertRepository.save(alert);

            log.info("Immediate notification logged: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to send immediate notification: {}", event.getEventId(), e);
        }
    }

    /**
     * 긴급 알림 전송 (HIGH 레벨)
     */
    @Override
    public void sendUrgentNotification(SecurityAlertEvent event) {
        log.warn("URGENT NOTIFICATION: {}", event.getDescription());

        try {
            Alert alert = Alert.builder()
                    .level(Alert.AlertLevel.HIGH)
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .sourceIp(event.getSourceIp())
                    .affectedResource(event.getAffectedResource())
                    .occurrenceCount(event.getOccurrenceCount())
                    .build();

            alertRepository.save(alert);

            log.info("Urgent notification logged: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to send urgent notification: {}", event.getEventId(), e);
        }
    }

    /**
     * 일반 알림 전송 (MEDIUM 레벨)
     */
    @Override
    public void sendStandardNotification(SecurityAlertEvent event) {
        log.info("STANDARD NOTIFICATION: {}", event.getDescription());

        try {
            Alert alert = Alert.builder()
                    .level(Alert.AlertLevel.MEDIUM)
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .sourceIp(event.getSourceIp())
                    .affectedResource(event.getAffectedResource())
                    .occurrenceCount(event.getOccurrenceCount())
                    .build();

            alertRepository.save(alert);

            log.debug("Standard notification logged: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to send standard notification: {}", event.getEventId(), e);
        }
    }

    /**
     * 알림 로그 저장 (LOW 레벨)
     */
    @Override
    public void logAlert(SecurityAlertEvent event) {
        log.info("ALERT LOG: {} - {}", event.getLevel(), event.getDescription());

        try {
            Alert alert = Alert.builder()
                    .level(Alert.AlertLevel.LOW)
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .sourceIp(event.getSourceIp())
                    .affectedResource(event.getAffectedResource())
                    .occurrenceCount(event.getOccurrenceCount())
                    .build();

            alertRepository.save(alert);

            log.debug("Alert logged: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to log alert: {}", event.getEventId(), e);
        }
    }

    /**
     * 알림 이력 저장
     */
    @Override
    public void saveAlertHistory(SecurityAlertEvent event) {
        log.debug("Saving alert history for event: {}", event.getEventId());

        try {
            Alert alert = Alert.builder()
                    .level(convertAlertLevel(event.getLevel()))
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .sourceIp(event.getSourceIp())
                    .affectedResource(event.getAffectedResource())
                    .occurrenceCount(event.getOccurrenceCount())
                    .build();

            alertRepository.save(alert);

            log.debug("Alert history saved: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Failed to save alert history: {}", event.getEventId(), e);
        }
    }

    /**
     * 의심스러운 활동 알림 생성
     */
    @Override
    public void createSuspiciousActivityAlert(AuditEvent event) {
        log.warn("SUSPICIOUS ACTIVITY: {} by user {}", event.getAction(), event.getUserId());

        try {
            Alert alert = Alert.builder()
                    .level(Alert.AlertLevel.HIGH)
                    .title("Suspicious Activity Detected")
                    .description(String.format("User %s performed suspicious action: %s on resource: %s",
                            event.getUsername(), event.getAction(), event.getResource()))
                    .sourceIp(event.getIpAddress())  // AuditEvent uses ipAddress, not sourceIp
                    .affectedResource(event.getResource())
                    .occurrenceCount(1)
                    .build();

            alertRepository.save(alert);

            log.info("Suspicious activity alert created: userId={}, action={}",
                    event.getUserId(), event.getAction());

        } catch (Exception e) {
            log.error("Failed to create suspicious activity alert: {}", event.getEventId(), e);
        }
    }

    /**
     * AttackDetectedEvent로부터 상세 설명 생성
     */
    private String buildDescription(AttackDetectedEvent event) {
        StringBuilder desc = new StringBuilder();

        desc.append("Attack Details:\n");
        desc.append(String.format("- Event ID: %s\n", event.getEventId()));
        desc.append(String.format("- Attack Type: %s\n", event.getAttackType()));
        desc.append(String.format("- Source IP: %s\n", event.getSourceIp()));
        desc.append(String.format("- Target URL: %s\n", event.getTargetUrl()));
        desc.append(String.format("- Risk Score: %d/100\n", event.getRiskScore()));
        desc.append(String.format("- Blocked: %s\n", event.getBlocked() ? "Yes" : "No"));

        if (event.getSignature() != null) {
            desc.append(String.format("- Signature: %s\n", event.getSignature()));
        }

        if (event.getPayload() != null && !event.getPayload().isEmpty()) {
            String truncatedPayload = event.getPayload().length() > 200
                    ? event.getPayload().substring(0, 200) + "..."
                    : event.getPayload();
            desc.append(String.format("- Payload: %s\n", truncatedPayload));
        }

        return desc.toString();
    }

    /**
     * SecurityAlertEvent.AlertLevel을 Alert.AlertLevel로 변환
     */
    private Alert.AlertLevel convertAlertLevel(SecurityAlertEvent.AlertLevel eventLevel) {
        if (eventLevel == null) {
            return Alert.AlertLevel.MEDIUM;
        }

        return switch (eventLevel) {
            case LOW -> Alert.AlertLevel.LOW;
            case MEDIUM -> Alert.AlertLevel.MEDIUM;
            case HIGH -> Alert.AlertLevel.HIGH;
            case CRITICAL -> Alert.AlertLevel.CRITICAL;
        };
    }
}
