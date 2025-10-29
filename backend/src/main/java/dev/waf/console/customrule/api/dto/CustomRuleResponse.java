package dev.waf.console.customrule.api.dto;

import dev.waf.console.customrule.domain.CustomRule;
import dev.waf.console.customrule.domain.RuleSeverity;
import dev.waf.console.customrule.domain.RuleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "커스텀 룰 응답")
public record CustomRuleResponse(
    @Schema(description = "룰 ID", example = "1")
    Long id,

    @Schema(description = "룰 이름", example = "SQL Injection 방어 룰")
    String name,

    @Schema(description = "룰 설명")
    String description,

    @Schema(description = "룰 내용")
    String ruleContent,

    @Schema(description = "룰 타입", example = "SQL_INJECTION")
    RuleType type,

    @Schema(description = "심각도", example = "HIGH")
    RuleSeverity severity,

    @Schema(description = "활성화 여부", example = "true")
    Boolean enabled,

    @Schema(description = "우선순위", example = "1000")
    Integer priority,

    @Schema(description = "대상 서비스")
    String targetService,

    @Schema(description = "대상 경로")
    String targetPath,

    @Schema(description = "생성자 정보")
    CreatedByInfo createdBy,

    @Schema(description = "생성 시간")
    LocalDateTime createdAt,

    @Schema(description = "수정 시간")
    LocalDateTime updatedAt,

    @Schema(description = "마지막 매칭 시간")
    LocalDateTime lastMatchedAt,

    @Schema(description = "매칭 횟수", example = "42")
    Long matchCount,

    @Schema(description = "차단 횟수", example = "35")
    Long blockCount
) {
    public record CreatedByInfo(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "이메일", example = "user@example.com")
        String email
    ) {
    }

    /**
     * 정적 팩토리 메서드 - CustomRule 엔티티로부터 응답 DTO 생성
     */
    public static CustomRuleResponse from(CustomRule rule) {
        CreatedByInfo createdByInfo = null;
        if (rule.getCreatedBy() != null) {
            createdByInfo = new CreatedByInfo(
                rule.getCreatedBy().getId(),
                rule.getCreatedBy().getName(),
                rule.getCreatedBy().getEmail()
            );
        }

        return new CustomRuleResponse(
            rule.getId(),
            rule.getName(),
            rule.getDescription(),
            rule.getRuleContent(),
            rule.getType(),
            rule.getSeverity(),
            rule.getEnabled(),
            rule.getPriority(),
            rule.getTargetService(),
            rule.getTargetPath(),
            createdByInfo,
            rule.getCreatedAt(),
            rule.getUpdatedAt(),
            rule.getLastMatchedAt(),
            rule.getMatchCount(),
            rule.getBlockCount()
        );
    }
}
