package dev.waf.console.core.domain.simulation;

public enum AttackType {
    SQL_INJECTION("SQL Injection", "' OR '1'='1"),
    XSS("Cross-Site Scripting", "<script>alert(1)</script>"),
    FILE_UPLOAD("Malicious File Upload", "test.php"),
    COMMAND_INJECTION("Command Injection", "; ls && cat /etc/passwd"),
    PATH_TRAVERSAL("Path Traversal", "../../../etc/passwd"),
    DVWA_NORMAL("DVWA Normal Request", "");

    private final String displayName;
    private final String defaultPayload;

    AttackType(String displayName, String defaultPayload) {
        this.displayName = displayName;
        this.defaultPayload = defaultPayload;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultPayload() {
        return defaultPayload;
    }

    public boolean isRefundCritical() {
        return this != DVWA_NORMAL;
    }
}