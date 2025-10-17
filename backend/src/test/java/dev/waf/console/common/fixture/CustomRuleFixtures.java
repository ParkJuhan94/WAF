package dev.waf.console.common.fixture;

import dev.waf.console.customrule.domain.CustomRule;
import dev.waf.console.customrule.domain.RuleSeverity;
import dev.waf.console.customrule.domain.RuleType;
import dev.waf.console.user.domain.User;

/**
 * CustomRule 엔티티 테스트 픽스처
 *
 * 테스트에서 재사용 가능한 CustomRule 객체를 생성합니다.
 */
public class CustomRuleFixtures {

    public static final String DEFAULT_RULE_NAME = "Test SQL Injection Block Rule";
    public static final String DEFAULT_DESCRIPTION = "Test rule for SQL injection detection and blocking";
    public static final String DEFAULT_RULE_CONTENT = "SecRule ARGS \"@rx (union|select|insert)\" \"id:9999,phase:2,block\"";

    /**
     * 기본 커스텀 룰 생성 (BLOCK 타입)
     */
    public static CustomRule createDefaultRule() {
        User creator = UserFixtures.createDefaultUser();
        return CustomRule.create(
            DEFAULT_RULE_NAME,
            DEFAULT_DESCRIPTION,
            DEFAULT_RULE_CONTENT,
            RuleType.BLOCK,
            RuleSeverity.HIGH,
            creator
        );
    }

    /**
     * 특정 RuleType의 커스텀 룰 생성
     */
    public static CustomRule createRuleWithType(RuleType type) {
        User creator = UserFixtures.createDefaultUser();
        return CustomRule.create(
            type.getDisplayName() + " Rule",
            "Test rule for " + type.getDisplayName(),
            DEFAULT_RULE_CONTENT,
            type,
            RuleSeverity.MEDIUM,
            creator
        );
    }

    /**
     * 특정 Severity의 커스텀 룰 생성
     */
    public static CustomRule createRuleWithSeverity(RuleSeverity severity) {
        User creator = UserFixtures.createDefaultUser();
        return CustomRule.create(
            DEFAULT_RULE_NAME,
            DEFAULT_DESCRIPTION,
            DEFAULT_RULE_CONTENT,
            RuleType.BLOCK,
            severity,
            creator
        );
    }

    /**
     * 완전히 커스터마이징된 룰 생성
     */
    public static CustomRule createCustomRule(
            String name,
            String description,
            String ruleContent,
            RuleType type,
            RuleSeverity severity,
            User creator
    ) {
        return CustomRule.create(name, description, ruleContent, type, severity, creator);
    }

    /**
     * 비활성화된 룰 생성
     */
    public static CustomRule createDisabledRule() {
        CustomRule rule = createDefaultRule();
        rule.disable();
        return rule;
    }

    /**
     * 활성화된 룰 생성
     */
    public static CustomRule createEnabledRule() {
        CustomRule rule = createDefaultRule();
        rule.enable();
        return rule;
    }

    /**
     * XSS 차단 룰 생성
     */
    public static CustomRule createXssRule() {
        User creator = UserFixtures.createDefaultUser();
        return CustomRule.create(
            "XSS Detection and Block Rule",
            "Detects and blocks cross-site scripting attacks",
            "SecRule ARGS \"@rx <script\" \"id:9998,phase:2,block\"",
            RuleType.BLOCK,
            RuleSeverity.HIGH,
            creator
        );
    }

    /**
     * Path Traversal 차단 룰 생성
     */
    public static CustomRule createPathTraversalRule() {
        User creator = UserFixtures.createDefaultUser();
        return CustomRule.create(
            "Path Traversal Block Rule",
            "Detects and blocks directory traversal attempts",
            "SecRule ARGS \"@rx \\.\\./\" \"id:9997,phase:2,block\"",
            RuleType.BLOCK,
            RuleSeverity.CRITICAL,
            creator
        );
    }

    /**
     * DENY 타입 룰 생성
     */
    public static CustomRule createDenyRule() {
        User creator = UserFixtures.createDefaultUser();
        return CustomRule.create(
            "Deny Suspicious Request Rule",
            "Denies suspicious requests and logs them",
            "SecRule REQUEST_URI \"@rx malicious\" \"id:9996,phase:1,deny\"",
            RuleType.DENY,
            RuleSeverity.MEDIUM,
            creator
        );
    }

    /**
     * RATE_LIMIT 타입 룰 생성
     */
    public static CustomRule createRateLimitRule() {
        User creator = UserFixtures.createDefaultUser();
        return CustomRule.create(
            "Rate Limiting Rule",
            "Limits request rate from same IP",
            "SecAction \"id:9995,phase:1,ratelimit:100/60\"",
            RuleType.RATE_LIMIT,
            RuleSeverity.LOW,
            creator
        );
    }

    /**
     * 매치 카운트가 있는 룰 생성
     */
    public static CustomRule createRuleWithMatches(int matchCount, int blockCount) {
        CustomRule rule = createDefaultRule();
        for (int i = 0; i < matchCount; i++) {
            rule.recordMatch();
        }
        for (int i = 0; i < blockCount; i++) {
            rule.recordBlock();
        }
        return rule;
    }

    /**
     * 여러 커스텀 룰 생성
     */
    public static CustomRule[] createMultipleRules(int count) {
        CustomRule[] rules = new CustomRule[count];
        User creator = UserFixtures.createDefaultUser();
        RuleType[] types = RuleType.values();

        for (int i = 0; i < count; i++) {
            RuleType type = types[i % types.length];
            rules[i] = CustomRule.create(
                "Test Rule " + i,
                "Test description " + i,
                DEFAULT_RULE_CONTENT,
                type,
                RuleSeverity.MEDIUM,
                creator
            );
        }
        return rules;
    }

    /**
     * 특정 타겟 스코프가 설정된 룰 생성
     */
    public static CustomRule createRuleWithTargetScope(String targetService, String targetPath) {
        CustomRule rule = createDefaultRule();
        rule.setTargetScope(targetService, targetPath);
        return rule;
    }
}
