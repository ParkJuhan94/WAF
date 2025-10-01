package dev.waf.console.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import dev.waf.console.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Elasticsearch 인덱싱 서비스
 *
 * WAF 이벤트를 Elasticsearch에 인덱싱하여 실시간 검색 및 분석 지원
 * - 공격 로그 인덱싱
 * - 접근 로그 인덱싱
 * - 알림 및 메트릭 인덱싱
 * - 실시간 검색 쿼리 지원
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexingService {

    private final ElasticsearchClient elasticsearchClient;

    private static final DateTimeFormatter INDEX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * 공격 탐지 이벤트 인덱싱
     */
    @Async
    public CompletableFuture<String> indexAttackEvent(AttackDetectedEvent event) {
        try {
            String indexName = "waf-attacks-" + formatIndexDate(event.getTimestamp());

            IndexRequest<AttackDetectedEvent> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(event.getEventId())
                .document(event)
            );

            IndexResponse response = elasticsearchClient.index(request);

            log.debug("Attack event indexed: index={}, id={}, result={}",
                indexName, response.id(), response.result());

            return CompletableFuture.completedFuture(response.id());

        } catch (IOException e) {
            log.error("Failed to index attack event: {}", event.getEventId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 접근 로그 이벤트 인덱싱
     */
    @Async
    public CompletableFuture<String> indexAccessLogEvent(AccessLogEvent event) {
        try {
            String indexName = "waf-access-" + formatIndexDate(event.getTimestamp());

            IndexRequest<AccessLogEvent> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(event.getEventId())
                .document(event)
            );

            IndexResponse response = elasticsearchClient.index(request);

            log.debug("Access log indexed: index={}, id={}, result={}",
                indexName, response.id(), response.result());

            return CompletableFuture.completedFuture(response.id());

        } catch (IOException e) {
            log.error("Failed to index access log: {}", event.getEventId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 보안 알림 이벤트 인덱싱
     */
    @Async
    public CompletableFuture<String> indexSecurityAlert(SecurityAlertEvent event) {
        try {
            String indexName = "waf-alerts-" + formatIndexDate(event.getTimestamp());

            IndexRequest<SecurityAlertEvent> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(event.getEventId())
                .document(event)
            );

            IndexResponse response = elasticsearchClient.index(request);

            log.debug("Security alert indexed: index={}, id={}, result={}",
                indexName, response.id(), response.result());

            return CompletableFuture.completedFuture(response.id());

        } catch (IOException e) {
            log.error("Failed to index security alert: {}", event.getEventId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 메트릭 이벤트 인덱싱
     */
    @Async
    public CompletableFuture<String> indexMetricsEvent(MetricsEvent event) {
        try {
            String indexName = "waf-metrics-" + formatIndexDate(event.getTimestamp());

            IndexRequest<MetricsEvent> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(event.getEventId())
                .document(event)
            );

            IndexResponse response = elasticsearchClient.index(request);

            log.debug("Metrics event indexed: index={}, id={}, result={}",
                indexName, response.id(), response.result());

            return CompletableFuture.completedFuture(response.id());

        } catch (IOException e) {
            log.error("Failed to index metrics event: {}", event.getEventId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 감사 로그 이벤트 인덱싱
     */
    @Async
    public CompletableFuture<String> indexAuditEvent(AuditEvent event) {
        try {
            String indexName = "waf-audit-" + formatIndexDate(event.getTimestamp());

            IndexRequest<AuditEvent> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(event.getEventId())
                .document(event)
            );

            IndexResponse response = elasticsearchClient.index(request);

            log.debug("Audit event indexed: index={}, id={}, result={}",
                indexName, response.id(), response.result());

            return CompletableFuture.completedFuture(response.id());

        } catch (IOException e) {
            log.error("Failed to index audit event: {}", event.getEventId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 공격 이벤트 검색
     *
     * @param sourceIp 소스 IP (null 가능)
     * @param attackType 공격 유형 (null 가능)
     * @param minRiskScore 최소 위험도 점수 (null 가능)
     * @param size 검색 결과 개수
     * @return 검색 결과 목록
     */
    public List<AttackDetectedEvent> searchAttackEvents(String sourceIp, String attackType,
                                                        Integer minRiskScore, int size) throws IOException {
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
            .index("waf-attacks-*")
            .size(size)
            .sort(s -> s.field(f -> f.field("timestamp").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)));

        // 검색 조건 추가
        searchBuilder.query(q -> q.bool(b -> {
            if (sourceIp != null) {
                b.must(m -> m.term(t -> t.field("sourceIp").value(sourceIp)));
            }
            if (attackType != null) {
                b.must(m -> m.term(t -> t.field("attackType").value(attackType)));
            }
            if (minRiskScore != null) {
                b.must(m -> m.range(r -> r.field("riskScore").gte(co.elastic.clients.json.JsonData.of(minRiskScore))));
            }
            return b;
        }));

        SearchResponse<AttackDetectedEvent> response = elasticsearchClient.search(
            searchBuilder.build(),
            AttackDetectedEvent.class
        );

        return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
    }

    /**
     * 접근 로그 검색
     *
     * @param clientIp 클라이언트 IP (null 가능)
     * @param statusCode HTTP 상태 코드 (null 가능)
     * @param size 검색 결과 개수
     * @return 검색 결과 목록
     */
    public List<AccessLogEvent> searchAccessLogs(String clientIp, Integer statusCode, int size) throws IOException {
        SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
            .index("waf-access-*")
            .size(size)
            .sort(s -> s.field(f -> f.field("timestamp").order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)));

        searchBuilder.query(q -> q.bool(b -> {
            if (clientIp != null) {
                b.must(m -> m.term(t -> t.field("clientIp").value(clientIp)));
            }
            if (statusCode != null) {
                b.must(m -> m.term(t -> t.field("statusCode").value(statusCode)));
            }
            return b;
        }));

        SearchResponse<AccessLogEvent> response = elasticsearchClient.search(
            searchBuilder.build(),
            AccessLogEvent.class
        );

        return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
    }

    /**
     * 공격 통계 집계
     *
     * @return 공격 유형별 통계
     */
    public Map<String, Long> getAttackStatistics() throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("waf-attacks-*")
            .size(0)
            .aggregations("attack_types", a -> a
                .terms(t -> t.field("attackType").size(50))
            )
        );

        SearchResponse<Void> response = elasticsearchClient.search(searchRequest, Void.class);

        if (response.aggregations() != null && response.aggregations().get("attack_types") != null) {
            var termsAgg = response.aggregations().get("attack_types").sterms();

            return termsAgg.buckets().array().stream()
                .collect(Collectors.toMap(
                    b -> b.key().stringValue(),
                    b -> b.docCount()
                ));
        }

        return Map.of();
    }

    /**
     * 날짜 포맷팅 (인덱스 이름용)
     */
    private String formatIndexDate(LocalDateTime timestamp) {
        return timestamp != null ? timestamp.format(INDEX_DATE_FORMAT) : LocalDateTime.now().format(INDEX_DATE_FORMAT);
    }
}
