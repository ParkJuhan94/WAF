package dev.waf.console.customrule.api.dto;

import dev.waf.console.customrule.domain.RuleSeverity;
import dev.waf.console.customrule.domain.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomRuleRequest {

    @NotBlank(message = "룰 이름은 필수입니다")
    @Size(max = 100, message = "룰 이름은 100자 이하여야 합니다")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;

    @NotBlank(message = "룰 내용은 필수입니다")
    private String ruleContent;

    @NotNull(message = "룰 타입은 필수입니다")
    private RuleType type;

    @NotNull(message = "심각도는 필수입니다")
    private RuleSeverity severity;

    private Integer priority = 1000;

    @Size(max = 50, message = "대상 서비스는 50자 이하여야 합니다")
    private String targetService;

    @Size(max = 200, message = "대상 경로는 200자 이하여야 합니다")
    private String targetPath;
}