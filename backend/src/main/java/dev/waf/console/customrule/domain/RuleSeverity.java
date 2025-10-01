package dev.waf.console.customrule.domain;

import lombok.Getter;

@Getter
public enum RuleSeverity {
    CRITICAL("치명적", "즉각적인 대응 필요", "#FF4444"),
    HIGH("높음", "빠른 대응 필요", "#FF8800"),
    MEDIUM("보통", "정기적인 모니터링", "#FFAA00"),
    LOW("낮음", "참고용", "#44AA44"),
    INFO("정보", "로깅 목적", "#4488FF");

    private final String displayName;
    private final String description;
    private final String color;

    RuleSeverity(String displayName, String description, String color) {
        this.displayName = displayName;
        this.description = description;
        this.color = color;
    }
}