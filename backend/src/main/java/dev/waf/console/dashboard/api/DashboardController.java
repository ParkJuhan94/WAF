package dev.waf.console.dashboard.api;

import dev.waf.console.common.dto.ApiResponse;
import dev.waf.console.dashboard.api.dto.AttackEventResponse;
import dev.waf.console.dashboard.api.dto.TrafficDataResponse;
import dev.waf.console.dashboard.api.dto.WAFStatsResponse;
import dev.waf.console.dashboard.api.dto.WAFStatusResponse;
import dev.waf.console.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 대시보드 API 컨트롤러
 *
 * WAF 대시보드에 필요한 통계, 상태, 트래픽 데이터를 제공하는 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "WAF 대시보드 데이터 조회 API")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * WAF 통계 조회
     *
     * @return WAF 통계 정보 (총 요청, 차단, 허용, 차단율, 응답시간, 가동시간)
     */
    @GetMapping("/stats")
    @Operation(
        summary = "WAF 통계 조회",
        description = "WAF의 전체 통계 정보를 조회합니다. 총 요청 수, 차단된 요청 수, 허용된 요청 수, 차단율, 평균 응답 시간, 서버 가동 시간을 포함합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "통계 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<WAFStatsResponse>> getStats() {
        log.debug("GET /api/dashboard/stats - Retrieving WAF statistics");

        try {
            WAFStatsResponse stats = dashboardService.getWAFStats();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            log.error("Failed to retrieve WAF stats", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("통계 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * WAF 상태 조회
     *
     * @return WAF 상태 정보 (시스템 상태, 버전, 재시작 시간, 설정 업데이트 시간, 룰 정보)
     */
    @GetMapping("/status")
    @Operation(
        summary = "WAF 상태 조회",
        description = "WAF의 현재 상태 정보를 조회합니다. 시스템 상태, 버전, 마지막 재시작 시간, 설정 업데이트 시간, 룰 개수를 포함합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<WAFStatusResponse>> getStatus() {
        log.debug("GET /api/dashboard/status - Retrieving WAF status");

        try {
            WAFStatusResponse status = dashboardService.getWAFStatus();
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("Failed to retrieve WAF status", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("상태 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 시간대별 트래픽 데이터 조회
     *
     * @param hours 조회할 시간 범위 (기본값: 24시간)
     * @return 시간대별 트래픽 데이터 목록
     */
    @GetMapping("/traffic")
    @Operation(
        summary = "트래픽 데이터 조회",
        description = "지정된 시간 범위 동안의 시간대별 트래픽 데이터를 조회합니다. 1시간 단위로 집계된 데이터를 반환합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "트래픽 데이터 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<List<TrafficDataResponse>>> getTraffic(
        @Parameter(description = "조회할 시간 범위 (시간 단위)", example = "24")
        @RequestParam(defaultValue = "24") int hours
    ) {
        log.debug("GET /api/dashboard/traffic?hours={} - Retrieving traffic data", hours);

        // 입력 검증
        if (hours <= 0 || hours > 168) { // 최대 1주일
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("시간 범위는 1~168 사이여야 합니다."));
        }

        try {
            List<TrafficDataResponse> traffic = dashboardService.getTrafficData(hours);
            return ResponseEntity.ok(ApiResponse.success(traffic));
        } catch (Exception e) {
            log.error("Failed to retrieve traffic data for hours={}", hours, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("트래픽 데이터 조회 실패: " + e.getMessage()));
        }
    }

    /**
     * 최근 공격 이벤트 조회
     *
     * @param limit 조회할 이벤트 개수 (기본값: 10개)
     * @return 최근 공격 이벤트 목록
     */
    @GetMapping("/attacks")
    @Operation(
        summary = "최근 공격 이벤트 조회",
        description = "최근 차단된 공격 이벤트를 조회합니다. 공격 유형, 출발 IP, 심각도 등의 정보를 포함합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공격 이벤트 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponse<List<AttackEventResponse>>> getAttacks(
        @Parameter(description = "조회할 이벤트 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.debug("GET /api/dashboard/attacks?limit={} - Retrieving recent attacks", limit);

        // 입력 검증
        if (limit <= 0 || limit > 100) { // 최대 100개
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("개수는 1~100 사이여야 합니다."));
        }

        try {
            List<AttackEventResponse> attacks = dashboardService.getRecentAttacks(limit);
            return ResponseEntity.ok(ApiResponse.success(attacks));
        } catch (Exception e) {
            log.error("Failed to retrieve recent attacks with limit={}", limit, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("공격 이벤트 조회 실패: " + e.getMessage()));
        }
    }
}
