package dev.waf.console.dashboard.service;

import dev.waf.console.dashboard.api.dto.AttackEventResponse;
import dev.waf.console.dashboard.api.dto.TrafficDataResponse;
import dev.waf.console.dashboard.api.dto.WAFStatsResponse;
import dev.waf.console.dashboard.api.dto.WAFStatusResponse;

import java.util.List;

/**
 * 대시보드 서비스 인터페이스
 *
 * WAF 대시보드에 필요한 통계, 상태, 트래픽 데이터를 제공
 */
public interface DashboardService {

    /**
     * WAF 통계 조회
     *
     * @return WAF 통계 정보
     */
    WAFStatsResponse getWAFStats();

    /**
     * WAF 상태 조회
     *
     * @return WAF 상태 정보
     */
    WAFStatusResponse getWAFStatus();

    /**
     * 시간대별 트래픽 데이터 조회
     *
     * @param hours 조회할 시간 범위 (기본 24시간)
     * @return 시간대별 트래픽 데이터 목록
     */
    List<TrafficDataResponse> getTrafficData(int hours);

    /**
     * 최근 공격 이벤트 조회
     *
     * @param limit 조회할 이벤트 개수 (기본 10개)
     * @return 최근 공격 이벤트 목록
     */
    List<AttackEventResponse> getRecentAttacks(int limit);
}
