package com.waf.api.refund.dto;

import com.waf.core.domain.simulation.AttackType;
import com.waf.core.domain.simulation.SimulationId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RefundValidationResponse {
    private String batchId;
    private ValidationStatus status;
    private RefundEligibility refundEligibility;
    private Map<AttackType, AttackTestResult> attackResults;
    private List<DvwaTestResult> dvwaResults;
    private ComplianceReport report;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public RefundValidationResponse() {}

    public static class AttackTestResult {
        private SimulationId simulationId;
        private boolean blocked;
        private int responseStatus;
        private long responseTime;
        private String ruleTriggered;
        private String screenshotUrl;
        private String wafLogId;
        private boolean compliant;
        private String notes;

        // Getters and Setters
        public SimulationId getSimulationId() { return simulationId; }
        public void setSimulationId(SimulationId simulationId) { this.simulationId = simulationId; }
        public boolean isBlocked() { return blocked; }
        public void setBlocked(boolean blocked) { this.blocked = blocked; }
        public int getResponseStatus() { return responseStatus; }
        public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
        public String getRuleTriggered() { return ruleTriggered; }
        public void setRuleTriggered(String ruleTriggered) { this.ruleTriggered = ruleTriggered; }
        public String getScreenshotUrl() { return screenshotUrl; }
        public void setScreenshotUrl(String screenshotUrl) { this.screenshotUrl = screenshotUrl; }
        public String getWafLogId() { return wafLogId; }
        public void setWafLogId(String wafLogId) { this.wafLogId = wafLogId; }
        public boolean isCompliant() { return compliant; }
        public void setCompliant(boolean compliant) { this.compliant = compliant; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class DvwaTestResult {
        private String scenario;
        private boolean passed;
        private int responseStatus;
        private long responseTime;
        private boolean blocked;
        private String notes;

        // Getters and Setters
        public String getScenario() { return scenario; }
        public void setScenario(String scenario) { this.scenario = scenario; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public int getResponseStatus() { return responseStatus; }
        public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
        public boolean isBlocked() { return blocked; }
        public void setBlocked(boolean blocked) { this.blocked = blocked; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class ComplianceReport {
        private int totalTests;
        private int successfulBlocks;
        private int failedBlocks;
        private int normalRequestsPassed;
        private int totalNormalRequests;
        private double complianceScore;
        private String pdfReportUrl;
        private LocalDateTime reportGeneratedAt;

        // Getters and Setters
        public int getTotalTests() { return totalTests; }
        public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
        public int getSuccessfulBlocks() { return successfulBlocks; }
        public void setSuccessfulBlocks(int successfulBlocks) { this.successfulBlocks = successfulBlocks; }
        public int getFailedBlocks() { return failedBlocks; }
        public void setFailedBlocks(int failedBlocks) { this.failedBlocks = failedBlocks; }
        public int getNormalRequestsPassed() { return normalRequestsPassed; }
        public void setNormalRequestsPassed(int normalRequestsPassed) { this.normalRequestsPassed = normalRequestsPassed; }
        public int getTotalNormalRequests() { return totalNormalRequests; }
        public void setTotalNormalRequests(int totalNormalRequests) { this.totalNormalRequests = totalNormalRequests; }
        public double getComplianceScore() { return complianceScore; }
        public void setComplianceScore(double complianceScore) { this.complianceScore = complianceScore; }
        public String getPdfReportUrl() { return pdfReportUrl; }
        public void setPdfReportUrl(String pdfReportUrl) { this.pdfReportUrl = pdfReportUrl; }
        public LocalDateTime getReportGeneratedAt() { return reportGeneratedAt; }
        public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) { this.reportGeneratedAt = reportGeneratedAt; }
    }

    public enum ValidationStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED
    }

    public enum RefundEligibility {
        ELIGIBLE,           // 환불 조건 충족
        NOT_ELIGIBLE,       // 환불 조건 미충족
        PARTIAL_ELIGIBLE,   // 부분 충족
        PENDING            // 아직 판단 불가
    }

    // Getters and Setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public ValidationStatus getStatus() { return status; }
    public void setStatus(ValidationStatus status) { this.status = status; }
    public RefundEligibility getRefundEligibility() { return refundEligibility; }
    public void setRefundEligibility(RefundEligibility refundEligibility) { this.refundEligibility = refundEligibility; }
    public Map<AttackType, AttackTestResult> getAttackResults() { return attackResults; }
    public void setAttackResults(Map<AttackType, AttackTestResult> attackResults) { this.attackResults = attackResults; }
    public List<DvwaTestResult> getDvwaResults() { return dvwaResults; }
    public void setDvwaResults(List<DvwaTestResult> dvwaResults) { this.dvwaResults = dvwaResults; }
    public ComplianceReport getReport() { return report; }
    public void setReport(ComplianceReport report) { this.report = report; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}