package dev.waf.console.core.domain.simulation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class TestResult {

    @Column(name = "result_blocked")
    private boolean blocked;

    @Column(name = "result_response_status")
    private int responseStatus;

    @Column(name = "result_response_time")
    private long responseTime;

    @Column(name = "result_rule_triggered")
    private String ruleTriggered;

    @Column(name = "result_screenshot_path")
    private String screenshotPath;

    @Column(name = "result_raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "result_waf_log_id")
    private String wafLogId;

    @Column(name = "result_timestamp")
    private LocalDateTime timestamp;

    protected TestResult() {
        // JPA 기본 생성자
    }

    public TestResult(boolean blocked, int responseStatus, long responseTime,
                     String ruleTriggered, String screenshotPath, String rawResponse,
                     String wafLogId) {
        this.blocked = blocked;
        this.responseStatus = responseStatus;
        this.responseTime = responseTime;
        this.ruleTriggered = ruleTriggered;
        this.screenshotPath = screenshotPath;
        this.rawResponse = rawResponse;
        this.wafLogId = wafLogId;
        this.timestamp = LocalDateTime.now();
    }

    public static TestResult blocked(int responseStatus, long responseTime, String ruleTriggered,
                                   String screenshotPath, String wafLogId) {
        return new TestResult(true, responseStatus, responseTime, ruleTriggered,
                            screenshotPath, null, wafLogId);
    }

    public static TestResult allowed(int responseStatus, long responseTime, String rawResponse) {
        return new TestResult(false, responseStatus, responseTime, null, null, rawResponse, null);
    }

    public boolean isCompliantForRefund() {
        // 환불 조건: 공격은 차단되어야 하고(403), 정상 요청은 허용되어야 함(200)
        if (responseStatus == 403 && blocked) {
            return true; // 공격 차단 성공
        }
        if (responseStatus == 200 && !blocked) {
            return true; // 정상 요청 허용 성공
        }
        return false;
    }

    // Getters
    public boolean isBlocked() { return blocked; }
    public int getResponseStatus() { return responseStatus; }
    public long getResponseTime() { return responseTime; }
    public String getRuleTriggered() { return ruleTriggered; }
    public String getScreenshotPath() { return screenshotPath; }
    public String getRawResponse() { return rawResponse; }
    public String getWafLogId() { return wafLogId; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestResult that = (TestResult) o;
        return blocked == that.blocked &&
               responseStatus == that.responseStatus &&
               responseTime == that.responseTime &&
               Objects.equals(ruleTriggered, that.ruleTriggered) &&
               Objects.equals(wafLogId, that.wafLogId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blocked, responseStatus, responseTime, ruleTriggered, wafLogId);
    }
}