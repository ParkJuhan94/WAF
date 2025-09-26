package dev.waf.console.core.repository;

import dev.waf.console.core.domain.simulation.AttackSimulation;
import dev.waf.console.core.domain.simulation.AttackType;
import dev.waf.console.core.domain.simulation.SimulationId;
import dev.waf.console.core.domain.simulation.SimulationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA 기반 SimulationRepository 구현체
 */
@Repository
public interface JpaSimulationRepository extends JpaRepository<AttackSimulation, String>, SimulationRepository {

    @Override
    default Optional<AttackSimulation> findById(SimulationId id) {
        return findById(id.getValue());
    }

    @Override
    List<AttackSimulation> findByExecutedBy(String executedBy);

    @Override
    List<AttackSimulation> findByStatus(SimulationStatus status);

    @Override
    @Query(value = "SELECT * FROM attack_simulation s WHERE s.attack_type = ?1 ORDER BY s.created_at DESC LIMIT ?2", nativeQuery = true)
    List<AttackSimulation> findByAttackTypeOrderByCreatedAtDesc(@Param("attackType") AttackType attackType,
        @Param("limit") int limit);

    @Override
    @Query("SELECT s FROM AttackSimulation s WHERE s.executedBy = :executedBy " +
        "AND s.createdAt BETWEEN :from AND :to " +
        "AND s.status = 'COMPLETED' " +
        "ORDER BY s.createdAt DESC")
    List<AttackSimulation> findRefundValidationBatch(@Param("executedBy") String executedBy,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);

    @Query("SELECT CASE WHEN COUNT(s) = :size AND " +
        "SUM(CASE WHEN s.status = 'COMPLETED' AND s.result.blocked = true THEN 1 ELSE 0 END) = :size " +
        "THEN true ELSE false END " +
        "FROM AttackSimulation s WHERE s.id.value IN :simulationIds")
    boolean isRefundCompliantByIds(@Param("simulationIds") List<String> simulationIds, @Param("size") int size);

    @Override
    default boolean isRefundCompliant(List<SimulationId> simulationIds) {
        List<String> ids = simulationIds.stream()
            .map(SimulationId::getValue)
            .toList();
        return isRefundCompliantByIds(ids, simulationIds.size());
    }

    @Override
    default void delete(SimulationId id) {
        deleteById(id.getValue());
    }

    @Override
    @Query("SELECT new dev.waf.console.core.repository.SimulationRepository$SimulationStats(" +
        "COUNT(s), " +
        "SUM(CASE WHEN s.status = 'COMPLETED' THEN 1 ELSE 0 END), " +
        "SUM(CASE WHEN s.status = 'FAILED' THEN 1 ELSE 0 END), " +
        "SUM(CASE WHEN s.status = 'COMPLETED' AND s.result.blocked = true THEN 1 ELSE 0 END), " +
        "AVG(s.result.responseTime)) " +
        "FROM AttackSimulation s WHERE s.executedBy = :executedBy " +
        "AND s.createdAt BETWEEN :from AND :to")
    SimulationStats getStatsByPeriod(@Param("executedBy") String executedBy,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
}