package dev.waf.console.core.domain.simulation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SimulationId {

    @Column(name = "id")
    private String value;

    protected SimulationId() {
        // JPA 기본 생성자
    }

    public SimulationId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SimulationId cannot be null or empty");
        }
        this.value = value;
    }

    public static SimulationId generate() {
        return new SimulationId(UUID.randomUUID().toString());
    }

    public static SimulationId of(String value) {
        return new SimulationId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimulationId that = (SimulationId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}