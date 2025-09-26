package dev.waf.console.core.domain.simulation;

public enum SimulationStatus {
    PENDING("테스트 대기 중"),
    RUNNING("테스트 실행 중"),
    COMPLETED("테스트 완료"),
    FAILED("테스트 실패"),
    CANCELLED("테스트 취소됨");

    private final String description;

    SimulationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isRunning() {
        return this == RUNNING;
    }
}