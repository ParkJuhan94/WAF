package dev.waf.console.customrule.api.dto;

import dev.waf.console.customrule.domain.RuleSeverity;
import dev.waf.console.customrule.domain.RuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "커스텀 룰 생성/수정 요청")
public record CustomRuleRequest(
    @NotBlank(message = "룰 이름은 필수입니다")
    @Size(max = 100, message = "룰 이름은 100자 이하여야 합니다")
    @Schema(description = "룰 이름", example = "SQL Injection 방어 룰")
    String name,

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    @Schema(description = "룰 설명", example = "SQL Injection 공격을 탐지하고 차단합니다")
    String description,

    @NotBlank(message = "룰 내용은 필수입니다")
    @Schema(description = "룰 내용 (ModSecurity 문법)", example = "SecRule ARGS \"@rx (?i:union.*select)\" \"id:1001,phase:2,deny,status:403\"")
    String ruleContent,

    @NotNull(message = "룰 타입은 필수입니다")
    @Schema(description = "룰 타입", example = "SQL_INJECTION")
    RuleType type,

    @NotNull(message = "심각도는 필수입니다")
    @Schema(description = "심각도", example = "HIGH")
    RuleSeverity severity,

    @Schema(description = "우선순위 (낮을수록 먼저 실행)", example = "1000")
    Integer priority,

    @Size(max = 50, message = "대상 서비스는 50자 이하여야 합니다")
    @Schema(description = "대상 서비스", example = "api-server")
    String targetService,

    @Size(max = 200, message = "대상 경로는 200자 이하여야 합니다")
    @Schema(description = "대상 경로", example = "/api/v1/users")
    String targetPath
) {
    public CustomRuleRequest {
        // Compact constructor - 기본값 설정
        if (priority == null) {
            priority = 1000;
        }
    }
}
