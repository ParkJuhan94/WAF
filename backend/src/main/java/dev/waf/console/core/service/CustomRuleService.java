package dev.waf.console.core.service;

import dev.waf.console.core.domain.rule.CustomRule;
import dev.waf.console.core.domain.rule.RuleSeverity;
import dev.waf.console.core.domain.rule.RuleType;
import dev.waf.console.core.domain.user.User;
import dev.waf.console.core.repository.CustomRuleRepository;
import dev.waf.console.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomRuleService {

    private final CustomRuleRepository customRuleRepository;
    private final UserRepository userRepository;

    /**
     * 모든 룰 조회 (페이징)
     */
    public Page<CustomRule> getAllRules(Pageable pageable) {
        return customRuleRepository.findAll(pageable);
    }

    /**
     * 필터링된 룰 조회
     */
    public Page<CustomRule> getRulesWithFilters(Boolean enabled, RuleType type,
                                               RuleSeverity severity, String targetService,
                                               Pageable pageable) {
        return customRuleRepository.findByFilters(enabled, type, severity, targetService, pageable);
    }

    /**
     * 키워드로 룰 검색
     */
    public Page<CustomRule> searchRules(String keyword, Pageable pageable) {
        return customRuleRepository.findByKeyword(keyword, pageable);
    }

    /**
     * 룰 단건 조회
     */
    public CustomRule getRule(Long id) {
        return customRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("룰을 찾을 수 없습니다: " + id));
    }

    /**
     * 사용자별 룰 조회
     */
    public Page<CustomRule> getUserRules(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return customRuleRepository.findByCreatedBy(user, pageable);
    }

    /**
     * 활성화된 룰 조회 (우선순위 순)
     */
    public List<CustomRule> getActiveRulesByPriority() {
        return customRuleRepository.findByEnabledTrueOrderByPriorityDesc();
    }

    /**
     * 특정 서비스에 적용 가능한 룰 조회
     */
    public List<CustomRule> getApplicableRules(String targetService) {
        return customRuleRepository.findApplicableRules(targetService);
    }

    /**
     * 최근 활성 룰 조회
     */
    public Page<CustomRule> getRecentlyActiveRules(Pageable pageable) {
        return customRuleRepository.findRecentlyActiveRules(pageable);
    }

    /**
     * 룰 생성
     */
    @Transactional
    public CustomRule createRule(String name, String description, String ruleContent,
                                RuleType type, RuleSeverity severity, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 중복 이름 체크
        if (customRuleRepository.existsByNameAndCreatedBy(name, user)) {
            throw new IllegalArgumentException("이미 존재하는 룰 이름입니다: " + name);
        }

        CustomRule rule = CustomRule.create(name, description, ruleContent, type, severity, user);
        CustomRule savedRule = customRuleRepository.save(rule);

        log.info("새로운 커스텀 룰이 생성되었습니다. ID: {}, Name: {}, User: {}",
                savedRule.getId(), savedRule.getName(), user.getEmail());

        return savedRule;
    }

    /**
     * 룰 수정
     */
    @Transactional
    public CustomRule updateRule(Long id, String name, String description, String ruleContent,
                                RuleType type, RuleSeverity severity, Integer priority, Long userId) {
        CustomRule rule = getRule(id);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 권한 체크 (본인이 생성한 룰만 수정 가능)
        if (!rule.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 룰만 수정할 수 있습니다.");
        }

        // 이름 중복 체크 (기존 룰 제외)
        if (!rule.getName().equals(name) && customRuleRepository.existsByNameAndCreatedBy(name, user)) {
            throw new IllegalArgumentException("이미 존재하는 룰 이름입니다: " + name);
        }

        rule.updateRule(name, description, ruleContent, type, severity, priority);
        CustomRule updatedRule = customRuleRepository.save(rule);

        log.info("커스텀 룰이 수정되었습니다. ID: {}, Name: {}", updatedRule.getId(), updatedRule.getName());

        return updatedRule;
    }

    /**
     * 룰 삭제
     */
    @Transactional
    public void deleteRule(Long id, Long userId) {
        CustomRule rule = getRule(id);

        // 권한 체크
        if (!rule.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 룰만 삭제할 수 있습니다.");
        }

        customRuleRepository.delete(rule);
        log.info("커스텀 룰이 삭제되었습니다. ID: {}, Name: {}", rule.getId(), rule.getName());
    }

    /**
     * 룰 활성화/비활성화
     */
    @Transactional
    public CustomRule toggleRuleStatus(Long id, Long userId) {
        CustomRule rule = getRule(id);

        // 권한 체크
        if (!rule.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 룰만 상태를 변경할 수 있습니다.");
        }

        if (rule.getEnabled()) {
            rule.disable();
        } else {
            rule.enable();
        }

        CustomRule updatedRule = customRuleRepository.save(rule);
        log.info("커스텀 룰 상태가 변경되었습니다. ID: {}, Enabled: {}",
                updatedRule.getId(), updatedRule.getEnabled());

        return updatedRule;
    }

    /**
     * 룰 대상 범위 설정
     */
    @Transactional
    public CustomRule setRuleTargetScope(Long id, String targetService, String targetPath, Long userId) {
        CustomRule rule = getRule(id);

        // 권한 체크
        if (!rule.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 생성한 룰만 수정할 수 있습니다.");
        }

        rule.setTargetScope(targetService, targetPath);
        return customRuleRepository.save(rule);
    }

    /**
     * 룰 매치 기록
     */
    @Transactional
    public void recordRuleMatch(Long id) {
        CustomRule rule = getRule(id);
        rule.recordMatch();
        customRuleRepository.save(rule);
    }

    /**
     * 룰 차단 기록
     */
    @Transactional
    public void recordRuleBlock(Long id) {
        CustomRule rule = getRule(id);
        rule.recordBlock();
        customRuleRepository.save(rule);
    }

    /**
     * 룰 통계 조회
     */
    public RuleStatistics getRuleStatistics() {
        long totalRules = customRuleRepository.count();
        long enabledRules = customRuleRepository.countByEnabled(true);
        long disabledRules = customRuleRepository.countByEnabled(false);
        Long totalMatches = customRuleRepository.getTotalMatchCount();
        Long totalBlocks = customRuleRepository.getTotalBlockCount();

        return RuleStatistics.builder()
                .totalRules(totalRules)
                .enabledRules(enabledRules)
                .disabledRules(disabledRules)
                .totalMatches(totalMatches != null ? totalMatches : 0L)
                .totalBlocks(totalBlocks != null ? totalBlocks : 0L)
                .build();
    }

    /**
     * 룰 통계 DTO
     */
    public static class RuleStatistics {
        private final long totalRules;
        private final long enabledRules;
        private final long disabledRules;
        private final long totalMatches;
        private final long totalBlocks;

        private RuleStatistics(long totalRules, long enabledRules, long disabledRules,
                              long totalMatches, long totalBlocks) {
            this.totalRules = totalRules;
            this.enabledRules = enabledRules;
            this.disabledRules = disabledRules;
            this.totalMatches = totalMatches;
            this.totalBlocks = totalBlocks;
        }

        public static RuleStatisticsBuilder builder() {
            return new RuleStatisticsBuilder();
        }

        // Getters
        public long getTotalRules() { return totalRules; }
        public long getEnabledRules() { return enabledRules; }
        public long getDisabledRules() { return disabledRules; }
        public long getTotalMatches() { return totalMatches; }
        public long getTotalBlocks() { return totalBlocks; }

        public static class RuleStatisticsBuilder {
            private long totalRules;
            private long enabledRules;
            private long disabledRules;
            private long totalMatches;
            private long totalBlocks;

            public RuleStatisticsBuilder totalRules(long totalRules) { this.totalRules = totalRules; return this; }
            public RuleStatisticsBuilder enabledRules(long enabledRules) { this.enabledRules = enabledRules; return this; }
            public RuleStatisticsBuilder disabledRules(long disabledRules) { this.disabledRules = disabledRules; return this; }
            public RuleStatisticsBuilder totalMatches(long totalMatches) { this.totalMatches = totalMatches; return this; }
            public RuleStatisticsBuilder totalBlocks(long totalBlocks) { this.totalBlocks = totalBlocks; return this; }

            public RuleStatistics build() {
                return new RuleStatistics(totalRules, enabledRules, disabledRules, totalMatches, totalBlocks);
            }
        }
    }
}