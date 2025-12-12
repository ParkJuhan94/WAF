package dev.waf.console.dashboard.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * WAF 상태 정보 응답 DTO
 */
@Schema(description = "WAF 상태 정보")
public record WAFStatusResponse(
    @Schema(description = "시스템 상태", example = "active", allowableValues = {"active", "inactive", "error", "maintenance"})
    String status,

    @Schema(description = "WAF 버전", example = "1.0.0")
    String version,

    @Schema(description = "마지막 재시작 시간", example = "2024-12-10T10:30:00")
    String lastRestart,

    @Schema(description = "설정 마지막 업데이트 시간", example = "2024-12-10T14:20:00")
    String configLastUpdated,

    @Schema(description = "총 룰 개수", example = "45")
    int rulesCount,

    @Schema(description = "활성화된 룰 개수", example = "38")
    int activeRulesCount
) {}
