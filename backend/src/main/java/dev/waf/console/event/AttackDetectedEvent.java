package dev.waf.console.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 공격 탐지 이벤트
 *
 * WAF가 악성 트래픽을 탐지했을 때 발생하는 이벤트
 * 실시간 모니터링 및 대응을 위한 핵심 정보 포함
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttackDetectedEvent extends WAFEvent {

    /**
     * 공격 타입
     */
    private AttackType attackType;

    /**
     * 공격자 IP 주소
     */
    private String sourceIp;

    /**
     * 대상 URL
     */
    private String targetUrl;

    /**
     * HTTP 메서드
     */
    private String httpMethod;

    /**
     * 사용자 에이전트
     */
    private String userAgent;

    /**
     * 공격 페이로드
     */
    private String payload;

    /**
     * 탐지한 WAF 룰 ID
     */
    private String ruleId;

    /**
     * 탐지한 WAF 룰 이름
     */
    private String ruleName;

    /**
     * 위험도 점수 (1-100)
     */
    private Integer riskScore;

    /**
     * 차단 여부
     */
    private Boolean blocked;

    /**
     * 응답 코드
     */
    private Integer responseCode;

    /**
     * 처리 시간 (ms)
     */
    private Long processingTime;

    /**
     * 지역 정보
     */
    private String geoLocation;

    /**
     * ISP 정보
     */
    private String isp;

    /**
     * 공격 패턴 시그니처
     */
    private String signature;

    @Override
    public WAFEventType getEventType() {
        return WAFEventType.ATTACK_DETECTED;
    }

    /**
     * 공격 타입 열거형
     */
    public enum AttackType {
        SQL_INJECTION("SQL 인젝션"),
        XSS("크로스 사이트 스크립팅"),
        COMMAND_INJECTION("명령어 인젝션"),
        PATH_TRAVERSAL("경로 순회"),
        FILE_UPLOAD("악성 파일 업로드"),
        CSRF("크로스 사이트 요청 위조"),
        SSRF("서버 사이드 요청 위조"),
        XXE("XML 외부 엔티티"),
        DESERIALIZATION("역직렬화 공격"),
        BRUTE_FORCE("브루트 포스"),
        DDoS("분산 서비스 거부"),
        BOT_ATTACK("봇 공격"),
        SCAN_ATTEMPT("스캔 시도"),
        SUSPICIOUS_ACTIVITY("의심스러운 활동"),
        UNKNOWN("알 수 없음");

        private final String description;

        AttackType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}