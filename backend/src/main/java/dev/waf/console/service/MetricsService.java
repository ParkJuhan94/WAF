package dev.waf.console.service;

import dev.waf.console.event.AccessLogEvent;
import dev.waf.console.event.AttackDetectedEvent;
import dev.waf.console.event.MetricsEvent;

public interface MetricsService {
    void updateAttackMetrics(AttackDetectedEvent event);
    void updateTrafficMetrics(AccessLogEvent event);
    void recordSlowResponse(AccessLogEvent event);
    void recordMetric(MetricsEvent event);
    void checkThresholds(MetricsEvent event);
}