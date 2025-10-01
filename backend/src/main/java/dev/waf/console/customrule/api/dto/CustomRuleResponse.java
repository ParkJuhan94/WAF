package dev.waf.console.customrule.api.dto;

import dev.waf.console.customrule.domain.CustomRule;
import dev.waf.console.customrule.domain.RuleSeverity;
import dev.waf.console.customrule.domain.RuleType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomRuleResponse {

    private Long id;
    private String name;
    private String description;
    private String ruleContent;
    private RuleType type;
    private RuleSeverity severity;
    private Boolean enabled;
    private Integer priority;
    private String targetService;
    private String targetPath;
    private CreatedByInfo createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastMatchedAt;
    private Long matchCount;
    private Long blockCount;

    @Data
    public static class CreatedByInfo {
        private Long id;
        private String name;
        private String email;
    }

    public static CustomRuleResponse from(CustomRule rule) {
        CustomRuleResponse response = new CustomRuleResponse();
        response.setId(rule.getId());
        response.setName(rule.getName());
        response.setDescription(rule.getDescription());
        response.setRuleContent(rule.getRuleContent());
        response.setType(rule.getType());
        response.setSeverity(rule.getSeverity());
        response.setEnabled(rule.getEnabled());
        response.setPriority(rule.getPriority());
        response.setTargetService(rule.getTargetService());
        response.setTargetPath(rule.getTargetPath());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        response.setLastMatchedAt(rule.getLastMatchedAt());
        response.setMatchCount(rule.getMatchCount());
        response.setBlockCount(rule.getBlockCount());

        // CreatedBy 정보 설정
        if (rule.getCreatedBy() != null) {
            CreatedByInfo createdByInfo = new CreatedByInfo();
            createdByInfo.setId(rule.getCreatedBy().getId());
            createdByInfo.setName(rule.getCreatedBy().getName());
            createdByInfo.setEmail(rule.getCreatedBy().getEmail());
            response.setCreatedBy(createdByInfo);
        }

        return response;
    }
}