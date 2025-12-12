package dev.waf.console.dashboard.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 공격 이벤트 응답 DTO
 */
@Schema(description = "공격 이벤트")
public record AttackEventResponse(
    @Schema(description = "이벤트 ID", example = "12345")
    String id,

    @Schema(description = "발생 시간", example = "2024-12-10T14:35:22")
    String timestamp,

    @Schema(description = "공격 출발 IP", example = "192.168.1.100")
    String sourceIp,

    @Schema(description = "공격 대상 경로", example = "/api/users")
    String targetPath,

    @Schema(description = "공격 유형", example = "SQL_INJECTION")
    String attackType,

    @Schema(description = "심각도", example = "high", allowableValues = {"low", "medium", "high", "critical"})
    String severity,

    @Schema(description = "차단 여부", example = "true")
    boolean blocked,

    @Schema(description = "User-Agent")
    String userAgent,

    @Schema(description = "공격 페이로드")
    String payload,

    @Schema(description = "매칭된 룰 목록")
    List<String> matchedRules
) {}
