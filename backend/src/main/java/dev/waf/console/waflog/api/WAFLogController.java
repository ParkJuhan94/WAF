package dev.waf.console.waflog.api;

import dev.waf.console.waflog.domain.WAFLog;
import dev.waf.console.waflog.service.WAFLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * WAF 로그 조회 API 컨트롤러
 *
 * WAF 로그 데이터 조회를 위한 REST API 제공
 * - 최근 성공/실패 로그 조회
 * - 상태별, 기간별 로그 필터링
 * - 로그 통계 정보 제공
 */
@Tag(name = "WAF Logs", description = "WAF 로그 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class WAFLogController {

    private final WAFLogService wafLogService;

    /**
     * 최근 로그 조회 (기본 500개)
     */
    @Operation(
        summary = "최근 로그 조회",
        description = "최근 WAF 로그를 조회합니다. 최대 500개까지 조회 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("/recent")
    public ResponseEntity<Page<WAFLog>> getRecentLogs(
        @Parameter(description = "조회할 로그 개수 (기본값: 500, 최대: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving recent {} logs", limit);

        if (limit <= 0 || limit > 500) {
            limit = 500;
        }

        Page<WAFLog> logs = wafLogService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 성공 로그만 조회
     */
    @Operation(
        summary = "성공 로그 조회",
        description = "성공적으로 처리된 요청의 로그만 조회합니다."
    )
    @GetMapping("/success")
    public ResponseEntity<Page<WAFLog>> getSuccessLogs(
        @Parameter(description = "조회할 로그 개수 (기본값: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving {} success logs", limit);
        Page<WAFLog> logs = wafLogService.getRecentSuccessLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 차단 로그만 조회
     */
    @Operation(
        summary = "차단 로그 조회",
        description = "WAF에 의해 차단된 요청의 로그만 조회합니다."
    )
    @GetMapping("/blocked")
    public ResponseEntity<Page<WAFLog>> getBlockedLogs(
        @Parameter(description = "조회할 로그 개수 (기본값: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving {} blocked logs", limit);
        Page<WAFLog> logs = wafLogService.getRecentBlockedLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 에러 로그만 조회
     */
    @Operation(
        summary = "에러 로그 조회",
        description = "처리 중 오류가 발생한 요청의 로그만 조회합니다."
    )
    @GetMapping("/errors")
    public ResponseEntity<Page<WAFLog>> getErrorLogs(
        @Parameter(description = "조회할 로그 개수 (기본값: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving {} error logs", limit);
        Page<WAFLog> logs = wafLogService.getRecentErrorLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 높은 위험도 로그 조회
     */
    @Operation(
        summary = "높은 위험도 로그 조회",
        description = "위험도 점수가 70 이상인 로그를 조회합니다."
    )
    @GetMapping("/high-risk")
    public ResponseEntity<Page<WAFLog>> getHighRiskLogs(
        @Parameter(description = "조회할 로그 개수 (기본값: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving {} high risk logs", limit);
        Page<WAFLog> logs = wafLogService.getHighRiskLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 상태별 로그 조회
     */
    @Operation(
        summary = "상태별 로그 조회",
        description = "특정 상태의 로그만 조회합니다."
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<WAFLog>> getLogsByStatus(
        @Parameter(description = "로그 상태 (SUCCESS, BLOCKED, ERROR, WARNING)")
        @PathVariable WAFLog.LogStatus status,
        @Parameter(description = "조회할 로그 개수 (기본값: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving {} logs with status: {}", limit, status);
        Page<WAFLog> logs = wafLogService.getRecentLogsByStatus(status, limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 기간별 로그 조회
     */
    @Operation(
        summary = "기간별 로그 조회",
        description = "특정 기간 내의 로그를 조회합니다."
    )
    @GetMapping("/range")
    public ResponseEntity<Page<WAFLog>> getLogsByTimeRange(
        @Parameter(description = "시작 시간 (yyyy-MM-dd'T'HH:mm:ss)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @Parameter(description = "종료 시간 (yyyy-MM-dd'T'HH:mm:ss)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
        @Parameter(description = "조회할 로그 개수 (기본값: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving {} logs from {} to {}", limit, startTime, endTime);

        if (startTime.isAfter(endTime)) {
            return ResponseEntity.badRequest().build();
        }

        Page<WAFLog> logs = wafLogService.getLogsByTimeRange(startTime, endTime, limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 로그 통계 조회
     */
    @Operation(
        summary = "로그 통계 조회",
        description = "최근 24시간 내 로그 통계 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "통계 조회 성공",
            content = @Content(schema = @Schema(implementation = WAFLogService.LogStatistics.class))
        )
    })
    @GetMapping("/statistics")
    public ResponseEntity<WAFLogService.LogStatistics> getLogStatistics() {
        log.info("Retrieving log statistics");
        WAFLogService.LogStatistics statistics = wafLogService.getLogStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 전체 로그 조회 (관리자용)
     */
    @Operation(
        summary = "전체 로그 조회",
        description = "모든 상태의 최근 로그를 조회합니다. 성공과 실패 로그를 모두 포함합니다."
    )
    @GetMapping("/all")
    public ResponseEntity<Page<WAFLog>> getAllLogs(
        @Parameter(description = "조회할 로그 개수 (기본값: 500)")
        @RequestParam(defaultValue = "500") int limit
    ) {
        log.info("Retrieving all {} logs", limit);
        Page<WAFLog> logs = wafLogService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * 특정 로그 상세 조회
     */
    @Operation(
        summary = "로그 상세 조회",
        description = "특정 ID의 로그 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<WAFLog> getLogById(
        @Parameter(description = "로그 ID")
        @PathVariable Long id
    ) {
        log.info("Retrieving log with ID: {}", id);
        // 구현 필요 시 WAFLogService에 findById 메서드 추가
        return ResponseEntity.notFound().build();
    }

    /**
     * 로그 개수 조회
     */
    @Operation(
        summary = "로그 개수 조회",
        description = "상태별 로그 개수를 간단히 조회합니다."
    )
    @GetMapping("/count")
    public ResponseEntity<LogCountResponse> getLogCount() {
        log.info("Retrieving log counts");
        WAFLogService.LogStatistics stats = wafLogService.getLogStatistics();

        LogCountResponse response = LogCountResponse.builder()
            .totalCount(stats.getTotalCount())
            .successCount(stats.getSuccessCount())
            .blockedCount(stats.getBlockedCount())
            .errorCount(stats.getErrorCount())
            .warningCount(stats.getWarningCount())
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 로그 개수 응답 DTO
     */
    @Schema(description = "로그 개수 응답")
    @lombok.Data
    @lombok.Builder
    public static class LogCountResponse {
        @Schema(description = "전체 로그 수")
        private long totalCount;

        @Schema(description = "성공 로그 수")
        private long successCount;

        @Schema(description = "차단 로그 수")
        private long blockedCount;

        @Schema(description = "에러 로그 수")
        private long errorCount;

        @Schema(description = "경고 로그 수")
        private long warningCount;
    }
}