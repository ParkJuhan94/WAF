package dev.waf.console.core.domain.simulation;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 환불 조건 검증을 위한 공격 시뮬레이션 도메인 DVWA 정상 요청 통과 + 5가지 공격 유형 차단 검증
 */
@Entity
@Table(name = "attack_simulation")
@Getter
public class AttackSimulation {

    @EmbeddedId
    private SimulationId id;

    @Column(name = "test_name")
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(name = "attack_type")
    private AttackType attackType;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "target_url")
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SimulationStatus status;

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Embedded
    private TestResult result;

    protected AttackSimulation() {
        // JPA 기본 생성자
    }

    public AttackSimulation(SimulationId id, String testName, String targetUrl,
        AttackType attackType, String payload, String executedBy) {
        if (id == null) {
            throw new IllegalArgumentException("SimulationId cannot be null");
        }
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Target URL cannot be null or empty");
        }
        if (attackType == null) {
            throw new IllegalArgumentException("AttackType cannot be null");
        }

        this.id = id;
        this.testName = testName != null ? testName : generateDefaultTestName(attackType);
        this.targetUrl = targetUrl;
        this.attackType = attackType;
        this.payload = payload != null ? payload : attackType.getDefaultPayload();
        this.executedBy = executedBy;
        this.createdAt = LocalDateTime.now();
        this.status = SimulationStatus.PENDING;
    }

    /**
     * 환불 조건 검증용 공격 시뮬레이션 생성
     */
    public static AttackSimulation forRefundValidation(String targetUrl, AttackType attackType, String executedBy) {
        return new AttackSimulation(
            SimulationId.generate(),
            "환불 조건 검증 - " + attackType.getDisplayName(),
            targetUrl,
            attackType,
            attackType.getDefaultPayload(),
            executedBy
        );
    }

    /**
     * DVWA 정상 요청 테스트용 시뮬레이션 생성
     */
    public static AttackSimulation forDvwaNormalTest(String dvwaUrl, String path, String executedBy) {
        return new AttackSimulation(
            SimulationId.generate(),
            "DVWA 정상 요청 테스트 - " + path,
            dvwaUrl + path,
            AttackType.DVWA_NORMAL,
            "",
            executedBy
        );
    }

    public void start() {
        if (status != SimulationStatus.PENDING) {
            throw new IllegalStateException("Cannot start simulation in " + status + " state");
        }
        this.status = SimulationStatus.RUNNING;
    }

    public void complete(TestResult result) {
        if (status != SimulationStatus.RUNNING) {
            throw new IllegalStateException("Cannot complete simulation in " + status + " state");
        }
        if (result == null) {
            throw new IllegalArgumentException("TestResult cannot be null");
        }

        this.result = result;
        this.status = SimulationStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot fail simulation in terminal state " + status);
        }

        this.status = SimulationStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        // 실패 사유는 별도 필드로 관리하거나 TestResult에 포함
    }

    /**
     * 환불 조건 준수 여부 확인
     */
    public boolean isCompliantForRefund() {
        if (status != SimulationStatus.COMPLETED || result == null) {
            return false;
        }

        return result.isCompliantForRefund();
    }

    private String generateDefaultTestName(AttackType attackType) {
        return "자동 시뮬레이션 - " + attackType.getDisplayName();
    }


}