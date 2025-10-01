package dev.waf.console.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditEvent extends WAFEvent {

    public enum AuditAction {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT, ACCESS, EXPORT
    }

    private AuditAction action;
    private String userId;
    private String username;
    private String resource;
    private String resourceId;
    private String details;
    private String ipAddress;
    private Boolean success;

    @Override
    public WAFEventType getEventType() {
        return WAFEventType.AUDIT;
    }
}