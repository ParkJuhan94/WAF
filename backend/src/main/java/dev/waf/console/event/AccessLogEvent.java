package dev.waf.console.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 접근 로그 이벤트
 *
 * 모든 HTTP 요청에 대한 로그 정보
 * 트래픽 분석 및 패턴 탐지에 활용
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccessLogEvent extends WAFEvent {

    private String clientIp;
    private String method;
    private String uri;
    private String protocol;
    private Integer statusCode;
    private Long responseSize;
    private Long responseTime;
    private String referer;
    private String userAgent;
    private String sessionId;
    private String userId;
    private Boolean cached;

    @Override
    public WAFEventType getEventType() {
        return WAFEventType.ACCESS_LOG;
    }
}