package dev.waf.console.customrule.domain;

import dev.waf.console.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleSeverity severity;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Integer priority = 1000;

    @Column(length = 50)
    private String targetService;

    @Column(length = 200)
    private String targetPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastMatchedAt;

    @Column(nullable = false)
    private Long matchCount = 0L;

    @Column(nullable = false)
    private Long blockCount = 0L;

    // Factory method
    public static CustomRule create(String name, String description, String ruleContent,
                                   RuleType type, RuleSeverity severity, User createdBy) {
        CustomRule rule = new CustomRule();
        rule.name = name;
        rule.description = description;
        rule.ruleContent = ruleContent;
        rule.type = type;
        rule.severity = severity;
        rule.enabled = true;
        rule.priority = type.getDefaultPriority();
        rule.createdBy = createdBy;
        rule.createdAt = LocalDateTime.now();
        rule.updatedAt = LocalDateTime.now();
        rule.matchCount = 0L;
        rule.blockCount = 0L;
        return rule;
    }

    // Business methods
    public void updateRule(String name, String description, String ruleContent,
                          RuleType type, RuleSeverity severity, Integer priority) {
        this.name = name;
        this.description = description;
        this.ruleContent = ruleContent;
        this.type = type;
        this.severity = severity;
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void setTargetScope(String targetService, String targetPath) {
        this.targetService = targetService;
        this.targetPath = targetPath;
        this.updatedAt = LocalDateTime.now();
    }

    public void recordMatch() {
        this.matchCount++;
        this.lastMatchedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void recordBlock() {
        this.blockCount++;
        this.lastMatchedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (matchCount == null) matchCount = 0L;
        if (blockCount == null) blockCount = 0L;
        if (enabled == null) enabled = true;
        if (priority == null) priority = 1000;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}