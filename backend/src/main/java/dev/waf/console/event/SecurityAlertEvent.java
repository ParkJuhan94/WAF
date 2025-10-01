package dev.waf.console.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SecurityAlertEvent extends WAFEvent {

    public enum AlertLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    private AlertLevel level;
    private String title;
    private String description;
    private String sourceIp;
    private String affectedResource;
    private Integer occurrenceCount;
    private Boolean acknowledged;

    @Override
    public WAFEventType getEventType() {
        return WAFEventType.SECURITY_ALERT;
    }
}