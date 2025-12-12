package dev.waf.console.dashboard.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * WAF 통계 정보 응답 DTO
 */
@Schema(description = "WAF 통계 정보")
public record WAFStatsResponse(
    @Schema(description = "총 요청 수", example = "15234")
    long totalRequests,

    @Schema(description = "차단된 요청 수", example = "342")
    long blockedRequests,

    @Schema(description = "허용된 요청 수", example = "14892")
    long allowedRequests,

    @Schema(description = "차단율 (%)", example = "2.24")
    double blockRate,

    @Schema(description = "평균 응답 시간 (ms)", example = "45.6")
    double avgResponseTime,

    @Schema(description = "서버 가동 시간 (초)", example = "3600")
    long uptime
) {}
