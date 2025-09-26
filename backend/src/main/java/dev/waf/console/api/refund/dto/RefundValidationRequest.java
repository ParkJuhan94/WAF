package dev.waf.console.api.refund.dto;

import dev.waf.console.core.domain.simulation.AttackType;

import java.util.List;
import java.util.Map;

public class RefundValidationRequest {
    private String targetUrl;
    private String dvwaUrl;
    private boolean includeScreenshots;
    private boolean generatePdfReport;
    private Map<AttackType, TestScenario> attackScenarios;
    private List<DvwaTestScenario> dvwaScenarios;

    public RefundValidationRequest() {}

    public RefundValidationRequest(String targetUrl, String dvwaUrl) {
        this.targetUrl = targetUrl;
        this.dvwaUrl = dvwaUrl;
        this.includeScreenshots = true;
        this.generatePdfReport = true;
    }

    public static class TestScenario {
        private boolean enabled;
        private String customPayload;

        public TestScenario() {}

        public TestScenario(boolean enabled, String customPayload) {
            this.enabled = enabled;
            this.customPayload = customPayload;
        }

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getCustomPayload() { return customPayload; }
        public void setCustomPayload(String customPayload) { this.customPayload = customPayload; }
    }

    public static class DvwaTestScenario {
        private String name;
        private String path;
        private String method;
        private Map<String, Object> parameters;

        public DvwaTestScenario() {}

        public DvwaTestScenario(String name, String path, String method) {
            this.name = name;
            this.path = path;
            this.method = method;
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    // Getters and Setters
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public String getDvwaUrl() { return dvwaUrl; }
    public void setDvwaUrl(String dvwaUrl) { this.dvwaUrl = dvwaUrl; }
    public boolean isIncludeScreenshots() { return includeScreenshots; }
    public void setIncludeScreenshots(boolean includeScreenshots) { this.includeScreenshots = includeScreenshots; }
    public boolean isGeneratePdfReport() { return generatePdfReport; }
    public void setGeneratePdfReport(boolean generatePdfReport) { this.generatePdfReport = generatePdfReport; }
    public Map<AttackType, TestScenario> getAttackScenarios() { return attackScenarios; }
    public void setAttackScenarios(Map<AttackType, TestScenario> attackScenarios) { this.attackScenarios = attackScenarios; }
    public List<DvwaTestScenario> getDvwaScenarios() { return dvwaScenarios; }
    public void setDvwaScenarios(List<DvwaTestScenario> dvwaScenarios) { this.dvwaScenarios = dvwaScenarios; }
}