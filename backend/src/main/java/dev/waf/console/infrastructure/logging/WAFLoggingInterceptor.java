package dev.waf.console.infrastructure.logging;

import dev.waf.console.waflog.domain.WAFLog;
import dev.waf.console.waflog.service.WAFLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * WAF 로깅 인터셉터
 *
 * 모든 HTTP 요청에 대해 WAF 로그를 자동으로 생성하고 저장
 * - 요청 시작 시간 기록
 * - 응답 완료 후 로그 저장
 * - WAF 처리 결과 분석 및 기록
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WAFLoggingInterceptor implements HandlerInterceptor {

    private final WAFLogService wafLogService;

    private static final String START_TIME_ATTRIBUTE = "waf.request.start.time";
    private static final String WAF_STATUS_HEADER = "X-WAF-Status";
    private static final String WAF_ATTACK_TYPE_HEADER = "X-WAF-Attack-Type";
    private static final String WAF_RISK_SCORE_HEADER = "X-WAF-Risk-Score";
    private static final String WAF_RULE_ID_HEADER = "X-WAF-Rule-ID";
    private static final String WAF_RULE_NAME_HEADER = "X-WAF-Rule-Name";
    private static final String WAF_BLOCK_REASON_HEADER = "X-WAF-Block-Reason";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 요청 시작 시간 기록
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 응답이 완료되기 전에는 처리하지 않음
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            // 로그 생성 및 저장
            createAndSaveLog(request, response, ex);
        } catch (Exception e) {
            log.error("Failed to save WAF log", e);
        }
    }

    private void createAndSaveLog(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        // 요청 처리 시간 계산
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long responseTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

        // 기본 요청 정보 추출
        String sourceIp = getClientIp(request);
        String httpMethod = request.getMethod();
        String requestUri = getFullRequestUri(request);
        String userAgent = request.getHeader("User-Agent");
        int responseStatusCode = response.getStatus();

        // WAF 처리 결과 분석
        WAFLog.LogStatus logStatus = determineLogStatus(response, ex);

        // WAF 헤더에서 추가 정보 추출
        String attackType = response.getHeader(WAF_ATTACK_TYPE_HEADER);
        String riskScoreHeader = response.getHeader(WAF_RISK_SCORE_HEADER);
        Integer riskScore = parseRiskScore(riskScoreHeader);
        String ruleId = response.getHeader(WAF_RULE_ID_HEADER);
        String ruleName = response.getHeader(WAF_RULE_NAME_HEADER);
        String blockReason = response.getHeader(WAF_BLOCK_REASON_HEADER);

        // 로그 엔티티 생성
        WAFLog wafLog = WAFLog.builder()
            .sourceIp(sourceIp)
            .httpMethod(httpMethod)
            .requestUri(requestUri)
            .userAgent(userAgent)
            .status(logStatus)
            .responseTimeMs(responseTime)
            .responseStatusCode(responseStatusCode)
            .attackType(attackType)
            .riskScore(riskScore)
            .ruleId(ruleId)
            .ruleName(ruleName)
            .blockReason(blockReason)
            .payloadSize(getPayloadSize(request))
            .geoCountry(extractCountryFromIp(sourceIp))
            .sessionId(request.getSession(false) != null ? request.getSession().getId() : null)
            .metadata(buildMetadata(request, response))
            .build();

        // 비동기로 로그 저장
        wafLogService.saveLogAsync(wafLog);

        log.debug("WAF log created: {} {} {} - Status: {}, Response Time: {}ms",
            httpMethod, requestUri, sourceIp, logStatus, responseTime);
    }

    /**
     * 로그 상태 결정
     */
    private WAFLog.LogStatus determineLogStatus(HttpServletResponse response, Exception ex) {
        // 예외가 발생한 경우
        if (ex != null) {
            return WAFLog.LogStatus.ERROR;
        }

        // WAF 상태 헤더 확인
        String wafStatus = response.getHeader(WAF_STATUS_HEADER);
        if (wafStatus != null) {
            switch (wafStatus.toUpperCase()) {
                case "BLOCKED":
                    return WAFLog.LogStatus.BLOCKED;
                case "WARNING":
                    return WAFLog.LogStatus.WARNING;
                case "SUCCESS":
                    return WAFLog.LogStatus.SUCCESS;
            }
        }

        // HTTP 상태 코드로 판단
        int statusCode = response.getStatus();
        if (statusCode >= 400) {
            if (statusCode == 403 || statusCode == 429) {
                return WAFLog.LogStatus.BLOCKED;
            } else {
                return WAFLog.LogStatus.ERROR;
            }
        }

        return WAFLog.LogStatus.SUCCESS;
    }

    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 전체 요청 URI 생성 (쿼리 스트링 포함)
     */
    private String getFullRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString != null && !queryString.isEmpty()) {
            return uri + "?" + queryString;
        }

        return uri;
    }

    /**
     * 위험도 점수 파싱
     */
    private Integer parseRiskScore(String riskScoreHeader) {
        if (riskScoreHeader == null || riskScoreHeader.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(riskScoreHeader);
        } catch (NumberFormatException e) {
            log.warn("Invalid risk score header: {}", riskScoreHeader);
            return null;
        }
    }

    /**
     * 요청 페이로드 크기 추출
     */
    private Long getPayloadSize(HttpServletRequest request) {
        String contentLength = request.getHeader("Content-Length");
        if (contentLength != null && !contentLength.isEmpty()) {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                log.warn("Invalid content length: {}", contentLength);
            }
        }
        return null;
    }

    /**
     * IP에서 국가 코드 추출 (간단한 구현, 실제로는 GeoIP 라이브러리 사용)
     */
    private String extractCountryFromIp(String ip) {
        // 실제 구현에서는 MaxMind GeoIP2 등의 라이브러리 사용
        if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return "KR"; // 로컬 IP는 한국으로 가정
        }
        return null; // 실제 GeoIP 조회 필요
    }

    /**
     * 메타데이터 구성 (JSON 형태)
     */
    private String buildMetadata(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder metadata = new StringBuilder("{");

        // 주요 헤더 정보 수집
        addMetadataField(metadata, "referer", request.getHeader("Referer"));
        addMetadataField(metadata, "accept", request.getHeader("Accept"));
        addMetadataField(metadata, "accept_language", request.getHeader("Accept-Language"));
        addMetadataField(metadata, "accept_encoding", request.getHeader("Accept-Encoding"));
        addMetadataField(metadata, "connection", request.getHeader("Connection"));

        // 응답 헤더 정보
        addMetadataField(metadata, "response_content_type", response.getHeader("Content-Type"));

        // 마지막 쉼표 제거
        if (metadata.length() > 1 && metadata.charAt(metadata.length() - 1) == ',') {
            metadata.setLength(metadata.length() - 1);
        }

        metadata.append("}");
        return metadata.toString();
    }

    private void addMetadataField(StringBuilder metadata, String key, String value) {
        if (value != null && !value.isEmpty()) {
            if (metadata.length() > 1) {
                metadata.append(",");
            }
            metadata.append("\"").append(key).append("\":\"")
                .append(value.replace("\"", "\\\"")).append("\"");
        }
    }
}