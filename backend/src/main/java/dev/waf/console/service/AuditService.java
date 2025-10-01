package dev.waf.console.service;

import dev.waf.console.event.AttackDetectedEvent;
import dev.waf.console.event.AuditEvent;

public interface AuditService {
    void logSecurityEvent(AttackDetectedEvent event);
    void saveAuditLog(AuditEvent event);
    boolean isSuspiciousActivity(AuditEvent event);
    void checkComplianceRules(AuditEvent event);
}