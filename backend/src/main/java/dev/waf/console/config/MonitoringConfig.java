package dev.waf.console.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 모니터링 및 메트릭 설정
 *
 * WAF Console의 포괄적인 모니터링 시스템:
 * - Prometheus 메트릭 수집 (Spring Boot Auto-configuration 사용)
 * - 커스텀 WAF 메트릭 정의
 * - 성능 모니터링 및 알림
 * - 분산 시스템 가시성
 *
 * 주요 메트릭:
 * - waf.attacks.detected: 탐지된 공격 수
 * - waf.traffic.requests: 총 요청 수
 * - waf.response.time: 응답 시간
 * - waf.rules.matched: 룰 매치 수
 * - waf.errors.count: 에러 발생 수
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Configuration
public class MonitoringConfig {

    /**
     * 개발/테스트용 Simple 메트릭 레지스트리
     */
    @Bean
    public SimpleMeterRegistry simpleMeterRegistry() {
        return new SimpleMeterRegistry();
    }

    /**
     * @Timed 애노테이션 지원을 위한 Aspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * WAF 전용 메트릭 서비스
     */
    @Bean
    public WAFMetricsService wafMetricsService(MeterRegistry meterRegistry) {
        return new WAFMetricsService(meterRegistry);
    }

    /**
     * 커스텀 태그 제공자 (HTTP 요청용)
     */
    @Bean
    public WebRequestTagProvider webRequestTagProvider() {
        return new WebRequestTagProvider();
    }

    public static class WebRequestTagProvider {
        public io.micrometer.core.instrument.Tags getCustomTags(HttpServletRequest request) {
            return io.micrometer.core.instrument.Tags.of(
                "client_type", isPublicIp(getClientIp(request)) ? "public" : "private",
                "waf_processed", request.getHeader("X-WAF-Processed") != null ? "true" : "false",
                "user_agent_type", categorizeUserAgent(request.getHeader("User-Agent"))
            );
        }

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

        private boolean isPublicIp(String ip) {
            if (ip == null || ip.isEmpty()) return false;

            return !(ip.startsWith("10.") ||
                    ip.startsWith("192.168.") ||
                    ip.startsWith("172.16.") ||
                    ip.equals("127.0.0.1") ||
                    ip.equals("::1"));
        }

        private String categorizeUserAgent(String userAgent) {
            if (userAgent == null || userAgent.isEmpty()) {
                return "unknown";
            }

            String ua = userAgent.toLowerCase();
            if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) {
                return "bot";
            } else if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
                return "mobile";
            } else if (ua.contains("chrome") || ua.contains("firefox") || ua.contains("safari")) {
                return "browser";
            } else {
                return "other";
            }
        }
    }

    /**
     * WAF 메트릭 서비스
     * WAF 관련 커스텀 메트릭 관리
     */
    public static class WAFMetricsService {
        private final MeterRegistry meterRegistry;

        // 카운터
        private final io.micrometer.core.instrument.Counter attacksDetectedCounter;
        private final io.micrometer.core.instrument.Counter requestsTotal;
        private final io.micrometer.core.instrument.Counter blockedRequestsCounter;

        // 타이머
        private final io.micrometer.core.instrument.Timer requestProcessingTimer;
        private final io.micrometer.core.instrument.Timer ruleEvaluationTimer;

        // 분포 요약
        private final io.micrometer.core.instrument.DistributionSummary payloadSizeDistribution;

        public WAFMetricsService(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;

            // 카운터 초기화
            this.attacksDetectedCounter = io.micrometer.core.instrument.Counter.builder("waf.attacks.detected")
                .description("Number of attacks detected by WAF")
                .register(meterRegistry);

            this.requestsTotal = io.micrometer.core.instrument.Counter.builder("waf.requests.total")
                .description("Total number of requests processed by WAF")
                .register(meterRegistry);

            this.blockedRequestsCounter = io.micrometer.core.instrument.Counter.builder("waf.requests.blocked")
                .description("Number of requests blocked by WAF")
                .register(meterRegistry);

            // 타이머 초기화
            this.requestProcessingTimer = io.micrometer.core.instrument.Timer.builder("waf.request.processing.time")
                .description("Time taken to process a request through WAF")
                .register(meterRegistry);

            this.ruleEvaluationTimer = io.micrometer.core.instrument.Timer.builder("waf.rule.evaluation.time")
                .description("Time taken to evaluate WAF rules")
                .register(meterRegistry);

            // 분포 요약 초기화
            this.payloadSizeDistribution = io.micrometer.core.instrument.DistributionSummary.builder("waf.payload.size")
                .description("Distribution of payload sizes in requests")
                .baseUnit("bytes")
                .register(meterRegistry);

            log.info("WAF metrics service initialized with custom metrics");
        }

        // 메트릭 기록 메서드들
        public void recordAttackDetected(String attackType, String severity) {
            io.micrometer.core.instrument.Counter.builder("waf.attacks.detected")
                .tag("attack_type", attackType)
                .tag("severity", severity)
                .register(meterRegistry)
                .increment();
        }

        public void recordRequest(String method, String status, boolean blocked) {
            io.micrometer.core.instrument.Counter.builder("waf.requests.total")
                .tag("method", method)
                .tag("status", status)
                .register(meterRegistry)
                .increment();

            if (blocked) {
                io.micrometer.core.instrument.Counter.builder("waf.requests.blocked")
                    .tag("method", method)
                    .register(meterRegistry)
                    .increment();
            }
        }

        public void recordRequestProcessingTime(long milliseconds, String ruleSet) {
            requestProcessingTimer.record(milliseconds, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        public void recordPayloadSize(long bytes) {
            payloadSizeDistribution.record(bytes);
        }
    }
}