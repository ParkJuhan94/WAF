package dev.waf.console.service;

import dev.waf.console.event.AttackDetectedEvent;
import dev.waf.console.event.AuditEvent;
import dev.waf.console.event.SecurityAlertEvent;

/**
 * 알림 서비스 인터페이스
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
public interface AlertService {
    void sendCriticalAlert(AttackDetectedEvent event);
    void requestAutoBlock(String sourceIp, AttackDetectedEvent.AttackType attackType);
    boolean isRepeatedAttacker(String sourceIp);
    void sendImmediateNotification(SecurityAlertEvent event);
    void sendUrgentNotification(SecurityAlertEvent event);
    void sendStandardNotification(SecurityAlertEvent event);
    void logAlert(SecurityAlertEvent event);
    void saveAlertHistory(SecurityAlertEvent event);
    void createSuspiciousActivityAlert(AuditEvent event);
}