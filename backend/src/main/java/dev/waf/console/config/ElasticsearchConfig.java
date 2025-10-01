package dev.waf.console.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 설정
 *
 * WAF 로그 데이터의 실시간 검색 및 분석을 위한 설정:
 * - 공격 로그 인덱싱 및 검색
 * - 실시간 로그 분석 및 집계
 * - 보안 이벤트 상관관계 분석
 * - 대시보드용 데이터 제공
 *
 * 인덱스 구조:
 * - waf-attacks-*: 공격 탐지 로그
 * - waf-access-*: 접근 로그
 * - waf-alerts-*: 보안 알림
 * - waf-metrics-*: 성능 메트릭
 * - waf-audit-*: 감사 로그
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "dev.waf.console.repository.search")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:1s}")
    private String connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:30s}")
    private String socketTimeout;

    /**
     * Elasticsearch REST 클라이언트 설정
     */
    @Bean
    public RestClient elasticsearchRestClient() {
        // URL 파싱
        String[] urlParts = elasticsearchUrl.replace("http://", "").replace("https://", "").split(":");
        String host = urlParts[0];
        int port = urlParts.length > 1 ? Integer.parseInt(urlParts[1]) : 9200;
        boolean isHttps = elasticsearchUrl.startsWith("https://");

        var restClientBuilder = RestClient.builder(
            new HttpHost(host, port, isHttps ? "https" : "http")
        );

        // 인증 설정
        if (!username.isEmpty() && !password.isEmpty()) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
            );

            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        // 타임아웃 설정
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder ->
            requestConfigBuilder
                .setConnectTimeout(parseTimeoutToMillis(connectionTimeout))
                .setSocketTimeout(parseTimeoutToMillis(socketTimeout))
        );

        var restClient = restClientBuilder.build();

        log.info("Elasticsearch REST client configured: host={}, port={}, auth={}",
            host, port, !username.isEmpty());

        return restClient;
    }

    /**
     * Elasticsearch 클라이언트 (새로운 Java API 클라이언트)
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );

        var client = new ElasticsearchClient(transport);

        log.info("Elasticsearch Java API client configured");
        return client;
    }

    /**
     * 인덱스 템플릿 매니저
     */
    @Bean
    public ElasticsearchIndexManager indexManager(ElasticsearchClient client) {
        return new ElasticsearchIndexManager(client);
    }

    /**
     * 시간 문자열을 밀리초로 변환
     */
    private int parseTimeoutToMillis(String timeout) {
        if (timeout.endsWith("s")) {
            return Integer.parseInt(timeout.substring(0, timeout.length() - 1)) * 1000;
        } else if (timeout.endsWith("ms")) {
            return Integer.parseInt(timeout.substring(0, timeout.length() - 2));
        } else {
            return Integer.parseInt(timeout);
        }
    }

    /**
     * Elasticsearch 인덱스 관리자
     * 인덱스 템플릿 및 매핑 설정 관리
     */
    public static class ElasticsearchIndexManager {
        private final ElasticsearchClient client;

        public ElasticsearchIndexManager(ElasticsearchClient client) {
            this.client = client;
            initializeIndexTemplates();
        }

        /**
         * 인덱스 템플릿 초기화
         */
        private void initializeIndexTemplates() {
            try {
                // WAF 공격 로그 템플릿
                createAttackLogsTemplate();

                // WAF 접근 로그 템플릿
                createAccessLogsTemplate();

                // WAF 알림 템플릿
                createAlertsTemplate();

                // WAF 메트릭 템플릿
                createMetricsTemplate();

                // WAF 감사 로그 템플릿
                createAuditTemplate();

                log.info("Elasticsearch index templates initialized successfully");

            } catch (Exception e) {
                log.error("Failed to initialize Elasticsearch index templates", e);
            }
        }

        /**
         * 공격 로그 인덱스 템플릿 생성
         */
        private void createAttackLogsTemplate() throws Exception {
            var templateBuilder = new co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest.Builder()
                .name("waf-attacks-template")
                .indexPatterns("waf-attacks-*")
                .template(t -> t
                    .settings(s -> s
                        .numberOfShards("3")
                        .numberOfReplicas("1")
                        .maxResultWindow(50000)
                        .refreshInterval(time -> time.time("5s"))
                    )
                    .mappings(m -> m
                        .properties("timestamp", p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
                        .properties("eventId", p -> p.keyword(k -> k))
                        .properties("attackType", p -> p.keyword(k -> k))
                        .properties("sourceIp", p -> p.ip(ip -> ip))
                        .properties("targetUrl", p -> p.text(txt -> txt.analyzer("standard")))
                        .properties("payload", p -> p.text(txt -> txt.analyzer("standard")))
                        .properties("riskScore", p -> p.integer(i -> i))
                        .properties("blocked", p -> p.boolean_(b -> b))
                        .properties("geoLocation", p -> p.geoPoint(geo -> geo))
                        .properties("signature", p -> p.keyword(k -> k))
                    )
                );

            client.indices().putIndexTemplate(templateBuilder.build());
            log.debug("Attack logs index template created");
        }

        /**
         * 접근 로그 인덱스 템플릿 생성
         */
        private void createAccessLogsTemplate() throws Exception {
            var templateBuilder = new co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest.Builder()
                .name("waf-access-template")
                .indexPatterns("waf-access-*")
                .template(t -> t
                    .settings(s -> s
                        .numberOfShards("6")  // 높은 처리량
                        .numberOfReplicas("1")
                        .refreshInterval(time -> time.time("10s"))  // 자주 갱신하지 않음
                    )
                    .mappings(m -> m
                        .properties("timestamp", p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
                        .properties("clientIp", p -> p.ip(ip -> ip))
                        .properties("method", p -> p.keyword(k -> k))
                        .properties("uri", p -> p.text(txt -> txt.analyzer("standard")))
                        .properties("statusCode", p -> p.integer(i -> i))
                        .properties("responseTime", p -> p.long_(l -> l))
                        .properties("responseSize", p -> p.long_(l -> l))
                        .properties("userAgent", p -> p.text(txt -> txt.analyzer("standard")))
                    )
                );

            client.indices().putIndexTemplate(templateBuilder.build());
            log.debug("Access logs index template created");
        }

        /**
         * 알림 인덱스 템플릿 생성
         */
        private void createAlertsTemplate() throws Exception {
            var templateBuilder = new co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest.Builder()
                .name("waf-alerts-template")
                .indexPatterns("waf-alerts-*")
                .template(t -> t
                    .settings(s -> s
                        .numberOfShards("2")
                        .numberOfReplicas("2")  // 높은 가용성
                        .refreshInterval(time -> time.time("1s"))  // 실시간 검색
                    )
                    .mappings(m -> m
                        .properties("timestamp", p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
                        .properties("level", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(txt -> txt.analyzer("standard")))
                        .properties("description", p -> p.text(txt -> txt.analyzer("standard")))
                        .properties("sourceIp", p -> p.ip(ip -> ip))
                        .properties("acknowledged", p -> p.boolean_(b -> b))
                    )
                );

            client.indices().putIndexTemplate(templateBuilder.build());
            log.debug("Alerts index template created");
        }

        /**
         * 메트릭 인덱스 템플릿 생성
         */
        private void createMetricsTemplate() throws Exception {
            var templateBuilder = new co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest.Builder()
                .name("waf-metrics-template")
                .indexPatterns("waf-metrics-*")
                .template(t -> t
                    .settings(s -> s
                        .numberOfShards("4")
                        .numberOfReplicas("1")
                        .refreshInterval(time -> time.time("30s"))  // 메트릭은 덜 빈번한 갱신
                    )
                    .mappings(m -> m
                        .properties("timestamp", p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
                        .properties("metricName", p -> p.keyword(k -> k))
                        .properties("value", p -> p.double_(d -> d))
                        .properties("unit", p -> p.keyword(k -> k))
                        .properties("tags", p -> p.object(o -> o.dynamic(co.elastic.clients.elasticsearch._types.mapping.DynamicMapping.True)))
                    )
                );

            client.indices().putIndexTemplate(templateBuilder.build());
            log.debug("Metrics index template created");
        }

        /**
         * 감사 로그 인덱스 템플릿 생성
         */
        private void createAuditTemplate() throws Exception {
            var templateBuilder = new co.elastic.clients.elasticsearch.indices.PutIndexTemplateRequest.Builder()
                .name("waf-audit-template")
                .indexPatterns("waf-audit-*")
                .template(t -> t
                    .settings(s -> s
                        .numberOfShards("2")
                        .numberOfReplicas("2")  // 감사 로그는 높은 내구성 필요
                        .refreshInterval(time -> time.time("30s"))
                    )
                    .mappings(m -> m
                        .properties("timestamp", p -> p.date(d -> d.format("strict_date_optional_time||epoch_millis")))
                        .properties("action", p -> p.keyword(k -> k))
                        .properties("userId", p -> p.keyword(k -> k))
                        .properties("username", p -> p.keyword(k -> k))
                        .properties("resource", p -> p.keyword(k -> k))
                        .properties("ipAddress", p -> p.ip(ip -> ip))
                        .properties("success", p -> p.boolean_(b -> b))
                        .properties("details", p -> p.text(txt -> txt.analyzer("standard")))
                    )
                );

            client.indices().putIndexTemplate(templateBuilder.build());
            log.debug("Audit index template created");
        }
    }
}