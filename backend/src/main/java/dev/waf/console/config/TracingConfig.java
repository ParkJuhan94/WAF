package dev.waf.console.config;

import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.api.common.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 분산 트레이싱 설정 (OpenTelemetry)
 *
 * 마이크로서비스 환경에서의 요청 추적:
 * - 요청 전체 생명주기 추적
 * - 서비스 간 호출 체인 가시화
 * - 성능 병목 지점 식별
 * - 에러 발생 지점 추적
 * - WAF 처리 단계별 상세 추적
 *
 * 트레이스 정보:
 * - span.waf.request: 전체 요청 처리
 * - span.waf.rule_evaluation: 룰 평가
 * - span.waf.threat_detection: 위협 탐지
 * - span.kafka.publish: 이벤트 발행
 * - span.elasticsearch.index: 로그 인덱싱
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Configuration
public class TracingConfig implements WebMvcConfigurer {

    @Value("${spring.application.name:waf-console}")
    private String serviceName;

    @Value("${management.tracing.zipkin.endpoint:http://localhost:9411/api/v2/spans}")
    private String zipkinEndpoint;

    @Value("${management.tracing.otlp.endpoint:}")
    private String otlpEndpoint;

    @Value("${management.tracing.enabled:true}")
    private boolean tracingEnabled;

    /**
     * OpenTelemetry 설정
     */
    @Bean
    public OpenTelemetry openTelemetry() {
        if (!tracingEnabled) {
            log.info("Tracing is disabled");
            return OpenTelemetry.noop();
        }

        // 리소스 정보 설정
        Resource resource = Resource.getDefault()
            .merge(Resource.create(
                Attributes.of(
                    AttributeKey.stringKey("service.name"), serviceName,
                    AttributeKey.stringKey("service.version"), getServiceVersion(),
                    AttributeKey.stringKey("deployment.environment"), getEnvironment()
                )
            ));

        // Span Exporter 설정
        SpanExporter spanExporter = createSpanExporter();

        // Tracer Provider 설정
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter)
                .setMaxExportBatchSize(512)
                .setScheduleDelay(Duration.ofSeconds(1))
                .build())
            .setResource(resource)
            .build();

        // OpenTelemetry SDK 구성
        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(ContextPropagators.create(
                io.opentelemetry.context.propagation.TextMapPropagator.composite(
                    W3CTraceContextPropagator.getInstance(),
                    io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator.getInstance()
                )
            ))
            .buildAndRegisterGlobal();

        log.info("OpenTelemetry configured: service={}, exporter={}",
            serviceName, spanExporter.getClass().getSimpleName());

        return openTelemetry;
    }

    /**
     * Micrometer Tracer (OpenTelemetry 브릿지)
     */
    @Bean
    public io.micrometer.tracing.Tracer micrometerTracer(OpenTelemetry openTelemetry) {
        return new OtelTracer(
            openTelemetry.getTracer(serviceName),
            new OtelCurrentTraceContext(),
            event -> {
                // 트레이스 이벤트 처리 (옵션)
                log.debug("Trace event: {}", event);
            }
        );
    }

    /**
     * WAF 트레이싱 인터셉터
     */
    @Bean
    public WAFTracingInterceptor wafTracingInterceptor(io.micrometer.tracing.Tracer tracer) {
        return new WAFTracingInterceptor(tracer);
    }

    /**
     * 인터셉터 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (tracingEnabled) {
            registry.addInterceptor(wafTracingInterceptor(null));
        }
    }

    /**
     * Span Exporter 생성
     */
    private SpanExporter createSpanExporter() {
        // OTLP 우선 사용 (Jaeger, OTEL Collector 등)
        if (!otlpEndpoint.isEmpty()) {
            log.info("Using OTLP span exporter: {}", otlpEndpoint);
            return OtlpHttpSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .setTimeout(Duration.ofSeconds(2))
                .build();
        }

        // Zipkin 사용 (기본값)
        log.info("Using Zipkin span exporter: {}", zipkinEndpoint);
        return ZipkinSpanExporter.builder()
            .setEndpoint(zipkinEndpoint)
            .build();
    }

    /**
     * 서비스 버전 조회
     */
    private String getServiceVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        return version != null ? version : "dev";
    }

    /**
     * 환경 정보 조회
     */
    private String getEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile != null ? profile : "development";
    }

    /**
     * WAF 트레이싱 인터셉터
     * HTTP 요청에 대한 상세 트레이싱 정보 추가
     */
    public static class WAFTracingInterceptor implements HandlerInterceptor {
        private final io.micrometer.tracing.Tracer tracer;

        public WAFTracingInterceptor(io.micrometer.tracing.Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                               Object handler) throws Exception {
            if (tracer == null) return true;

            // WAF 요청 처리 span 시작
            io.micrometer.tracing.Span span = tracer.nextSpan()
                .name("waf.request.processing")
                .tag("http.method", request.getMethod())
                .tag("http.url", request.getRequestURL().toString())
                .tag("http.user_agent", request.getHeader("User-Agent"))
                .tag("client.ip", getClientIp(request))
                .start();

            // 요청 컨텍스트에 span 저장
            request.setAttribute("waf.trace.span", span);

            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                  Object handler, Exception ex) throws Exception {
            if (tracer == null) return;

            io.micrometer.tracing.Span span =
                (io.micrometer.tracing.Span) request.getAttribute("waf.trace.span");

            if (span != null) {
                // 응답 정보 추가
                span.tag("http.status_code", String.valueOf(response.getStatus()));

                // WAF 처리 결과 추가
                String wafResult = response.getHeader("X-WAF-Status");
                if (wafResult != null) {
                    span.tag("waf.status", wafResult);
                }

                String blockedReason = response.getHeader("X-WAF-Block-Reason");
                if (blockedReason != null) {
                    span.tag("waf.block_reason", blockedReason);
                }

                // 에러 정보 추가
                if (ex != null) {
                    span.tag("error", true);
                    span.tag("error.type", ex.getClass().getSimpleName());
                    span.tag("error.message", ex.getMessage());
                }

                // span 종료
                span.end();
            }
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
    }

    /**
     * 커스텀 트레이싱 서비스
     * 비즈니스 로직에서 사용할 트레이싱 유틸리티
     */
    @Bean
    public WAFTracingService wafTracingService(io.micrometer.tracing.Tracer tracer) {
        return new WAFTracingService(tracer);
    }

    /**
     * WAF 트레이싱 서비스
     * WAF 특화 트레이싱 기능 제공
     */
    public static class WAFTracingService {
        private final io.micrometer.tracing.Tracer tracer;

        public WAFTracingService(io.micrometer.tracing.Tracer tracer) {
            this.tracer = tracer;
        }

        /**
         * 룰 평가 트레이싱
         */
        public io.micrometer.tracing.Span startRuleEvaluationSpan(String ruleId, String ruleName) {
            return tracer.nextSpan()
                .name("waf.rule.evaluation")
                .tag("rule.id", ruleId)
                .tag("rule.name", ruleName)
                .start();
        }

        /**
         * 위협 탐지 트레이싱
         */
        public io.micrometer.tracing.Span startThreatDetectionSpan(String attackType, String sourceIp) {
            return tracer.nextSpan()
                .name("waf.threat.detection")
                .tag("attack.type", attackType)
                .tag("source.ip", sourceIp)
                .start();
        }

        /**
         * Kafka 이벤트 발행 트레이싱
         */
        public io.micrometer.tracing.Span startKafkaPublishSpan(String topic, String eventType) {
            return tracer.nextSpan()
                .name("kafka.publish")
                .tag("kafka.topic", topic)
                .tag("event.type", eventType)
                .start();
        }

        /**
         * Elasticsearch 인덱싱 트레이싱
         */
        public io.micrometer.tracing.Span startElasticsearchIndexSpan(String index, String operation) {
            return tracer.nextSpan()
                .name("elasticsearch.index")
                .tag("es.index", index)
                .tag("es.operation", operation)
                .start();
        }

        /**
         * 데이터베이스 조회 트레이싱
         */
        public io.micrometer.tracing.Span startDatabaseQuerySpan(String query, String table) {
            return tracer.nextSpan()
                .name("database.query")
                .tag("db.statement", query)
                .tag("db.table", table)
                .start();
        }

        /**
         * 외부 API 호출 트레이싱
         */
        public io.micrometer.tracing.Span startExternalApiSpan(String apiName, String endpoint) {
            return tracer.nextSpan()
                .name("external.api.call")
                .tag("api.name", apiName)
                .tag("api.endpoint", endpoint)
                .start();
        }
    }
}