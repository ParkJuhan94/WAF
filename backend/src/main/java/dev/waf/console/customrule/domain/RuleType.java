package dev.waf.console.customrule.domain;

import lombok.Getter;

@Getter
public enum RuleType {
    BLOCK("차단", "요청을 완전히 차단", 900),
    DENY("거부", "요청을 거부하고 로깅", 800),
    DROP("드롭", "응답 없이 연결 종료", 700),
    REDIRECT("리다이렉트", "다른 페이지로 리다이렉트", 600),
    LOG("로깅", "로그만 기록", 500),
    RATE_LIMIT("속도 제한", "요청 속도 제한", 400),
    CUSTOM("사용자 정의", "사용자 정의 규칙", 1000);

    private final String displayName;
    private final String description;
    private final int defaultPriority;

    RuleType(String displayName, String description, int defaultPriority) {
        this.displayName = displayName;
        this.description = description;
        this.defaultPriority = defaultPriority;
    }
}