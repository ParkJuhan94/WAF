package dev.waf.console.dashboard.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 트래픽 데이터 응답 DTO
 */
@Schema(description = "트래픽 데이터")
public record TrafficDataResponse(
    @Schema(description = "타임스탬프", example = "2024-12-10T14:00:00")
    String timestamp,

    @Schema(description = "총 요청 수", example = "234")
    long totalRequests,

    @Schema(description = "차단된 요청 수", example = "12")
    long blockedRequests,

    @Schema(description = "허용된 요청 수", example = "222")
    long allowedRequests,

    @Schema(description = "평균 응답 시간 (ms)", example = "42.5")
    double responseTime
) {}
