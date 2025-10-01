package dev.waf.console.customrule.repository;

import dev.waf.console.customrule.domain.CustomRule;
import dev.waf.console.customrule.domain.RuleSeverity;
import dev.waf.console.customrule.domain.RuleType;
import dev.waf.console.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomRuleRepository extends JpaRepository<CustomRule, Long> {

    // 기본 조회
    List<CustomRule> findByEnabledTrue();

    List<CustomRule> findByCreatedBy(User createdBy);

    Page<CustomRule> findByCreatedBy(User createdBy, Pageable pageable);

    // 필터링 조회
    Page<CustomRule> findByEnabledAndType(Boolean enabled, RuleType type, Pageable pageable);

    Page<CustomRule> findByEnabledAndSeverity(Boolean enabled, RuleSeverity severity, Pageable pageable);

    Page<CustomRule> findByTargetService(String targetService, Pageable pageable);

    @Query("SELECT r FROM CustomRule r WHERE r.enabled = :enabled AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:severity IS NULL OR r.severity = :severity) AND " +
           "(:targetService IS NULL OR r.targetService = :targetService)")
    Page<CustomRule> findByFilters(@Param("enabled") Boolean enabled,
                                  @Param("type") RuleType type,
                                  @Param("severity") RuleSeverity severity,
                                  @Param("targetService") String targetService,
                                  Pageable pageable);

    // 검색
    @Query("SELECT r FROM CustomRule r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<CustomRule> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 통계
    long countByEnabled(Boolean enabled);

    long countByType(RuleType type);

    long countBySeverity(RuleSeverity severity);

    @Query("SELECT SUM(r.matchCount) FROM CustomRule r WHERE r.enabled = true")
    Long getTotalMatchCount();

    @Query("SELECT SUM(r.blockCount) FROM CustomRule r WHERE r.enabled = true")
    Long getTotalBlockCount();

    // 최근 활성 룰
    @Query("SELECT r FROM CustomRule r WHERE r.enabled = true AND r.lastMatchedAt IS NOT NULL " +
           "ORDER BY r.lastMatchedAt DESC")
    Page<CustomRule> findRecentlyActiveRules(Pageable pageable);

    // 우선순위별 조회
    List<CustomRule> findByEnabledTrueOrderByPriorityDesc();

    // 특정 서비스의 룰 조회 (우선순위 순)
    @Query("SELECT r FROM CustomRule r WHERE r.enabled = true AND " +
           "(:targetService IS NULL OR r.targetService = :targetService OR r.targetService IS NULL) " +
           "ORDER BY r.priority DESC")
    List<CustomRule> findApplicableRules(@Param("targetService") String targetService);

    // 중복 룰 체크
    Optional<CustomRule> findByNameAndCreatedBy(String name, User createdBy);

    boolean existsByNameAndCreatedBy(String name, User createdBy);

    // 최근 생성된 룰
    List<CustomRule> findTop10ByOrderByCreatedAtDesc();

    // 사용자별 룰 수
    @Query("SELECT COUNT(r) FROM CustomRule r WHERE r.createdBy = :user")
    long countByCreatedBy(@Param("user") User user);

    // 특정 기간 동안 생성된 룰
    @Query("SELECT r FROM CustomRule r WHERE r.createdAt BETWEEN :startDate AND :endDate")
    List<CustomRule> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}