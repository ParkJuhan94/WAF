package com.waf.core.repository;

import com.waf.core.domain.simulation.AttackSimulation;
import com.waf.core.domain.simulation.AttackType;
import com.waf.core.domain.simulation.SimulationId;
import com.waf.core.domain.simulation.SimulationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 환불 조건 검증용 공격 시뮬레이션 Repository 인터페이스
 */
public interface SimulationRepository {

    /**
     * 시뮬레이션 저장
     */
    SimulationId save(AttackSimulation simulation);

    /**
     * ID로 시뮬레이션 조회
     */
    Optional<AttackSimulation> findById(SimulationId id);

    /**
     * 사용자별 시뮬레이션 목록 조회
     */
    List<AttackSimulation> findByExecutedBy(String executedBy);

    /**
     * 상태별 시뮬레이션 조회
     */
    List<AttackSimulation> findByStatus(SimulationStatus status);

    /**
     * 공격 유형별 최근 시뮬레이션 조회
     */
    List<AttackSimulation> findByAttackTypeOrderByCreatedAtDesc(AttackType attackType, int limit);

    /**
     * 환불 조건 검증용 시뮬레이션 배치 조회
     * - 5가지 공격 유형 + DVWA 정상 요청이 모두 완료된 배치
     */
    List<AttackSimulation> findRefundValidationBatch(String executedBy, LocalDateTime from, LocalDateTime to);

    /**
     * 완료된 시뮬레이션 중 환불 조건 준수 여부 확인
     */
    boolean isRefundCompliant(List<SimulationId> simulationIds);

    /**
     * 시뮬레이션 삭제 (실제 삭제 또는 논리 삭제)
     */
    void delete(SimulationId id);

    /**
     * 기간별 시뮬레이션 통계
     */
    SimulationStats getStatsByPeriod(String executedBy, LocalDateTime from, LocalDateTime to);

    /**
     * 시뮬레이션 통계 정보
     */
    class SimulationStats {
        private final long totalCount;
        private final long completedCount;
        private final long failedCount;
        private final long refundCompliantCount;
        private final double averageResponseTime;

        public SimulationStats(long totalCount, long completedCount, long failedCount,
                             long refundCompliantCount, double averageResponseTime) {
            this.totalCount = totalCount;
            this.completedCount = completedCount;
            this.failedCount = failedCount;
            this.refundCompliantCount = refundCompliantCount;
            this.averageResponseTime = averageResponseTime;
        }

        public long getTotalCount() { return totalCount; }
        public long getCompletedCount() { return completedCount; }
        public long getFailedCount() { return failedCount; }
        public long getRefundCompliantCount() { return refundCompliantCount; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public double getSuccessRate() {
            return totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
        }
        public double getRefundComplianceRate() {
            return completedCount > 0 ? (double) refundCompliantCount / completedCount * 100 : 0;
        }
    }
}