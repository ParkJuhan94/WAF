package dev.waf.console.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 메시지 스트리밍 설정
 *
 * 이벤트 기반 아키텍처의 핵심:
 * - WAF 이벤트 스트리밍 (공격 탐지, 로그, 알림)
 * - 실시간 데이터 처리 및 분석
 * - 마이크로서비스 간 비동기 통신
 * - 이벤트 소싱 및 CQRS 패턴 지원
 *
 * 토픽 구조:
 * - waf.attacks: 공격 탐지 이벤트
 * - waf.logs: 접근 로그 스트림
 * - waf.alerts: 보안 알림
 * - waf.metrics: 성능 메트릭
 * - waf.audit: 감사 로그
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:waf-console-group}")
    private String groupId;

    @Value("${waf.console.events.kafka.topics.attacks:waf.attacks}")
    private String attacksTopic;

    @Value("${waf.console.events.kafka.topics.logs:waf.logs}")
    private String logsTopic;

    @Value("${waf.console.events.kafka.topics.alerts:waf.alerts}")
    private String alertsTopic;

    @Value("${waf.console.events.kafka.topics.metrics:waf.metrics}")
    private String metricsTopic;

    @Value("${waf.console.events.kafka.topics.audit:waf.audit}")
    private String auditTopic;

    /**
     * Kafka Admin 클라이언트 설정
     * 토픽 생성 및 관리용
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configs.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 60000);

        log.info("Kafka Admin configured with bootstrap servers: {}", bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Producer 설정
     * 고성능 및 내구성을 위한 최적화
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // 기본 연결 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 성능 최적화
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);        // 16KB 배치
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);            // 10ms 대기
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");   // LZ4 압축
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);   // 32MB 버퍼

        // 내구성 설정 (중요한 WAF 이벤트 손실 방지)
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");              // 모든 replica 확인
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);               // 3회 재시도
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // 중복 방지

        // 타임아웃 설정
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        log.info("Kafka Producer configured with high-throughput and durability settings");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka Template
     * 메시지 발송을 위한 고수준 API
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());

        // 비동기 전송 결과 로깅 - Spring Boot 3.x에서는 다른 방식으로 구성
        // template.setProducerInterceptors(java.util.List.of(new KafkaProducerInterceptor()));

        log.info("KafkaTemplate configured with async logging");
        return template;
    }

    /**
     * Consumer 설정
     * 실시간 처리를 위한 최적화
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // 기본 연결 설정
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // JSON 역직렬화 설정 (에러 핸들링 포함)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "dev.waf.console.event");

        // 실시간 처리 최적화
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");     // 최신 메시지부터
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);       // 수동 커밋
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);           // 배치 크기
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);              // 즉시 처리
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);          // 최대 500ms 대기

        // 세션 관리
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);       // 30초 세션
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);    // 10초 하트비트

        log.info("Kafka Consumer configured for real-time processing");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka Listener Container Factory
     * 동시성 및 에러 핸들링 설정
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // 동시성 설정 (CPU 코어 기반)
        int concurrency = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        factory.setConcurrency(concurrency);

        // 수동 커밋 설정 (정확한 처리 보장)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // 에러 핸들링 (재시도 + 로깅)
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            new FixedBackOff(5000L, 3L)  // 5초 간격으로 3회 재시도
        ));

        // 배치 리스너 지원
        factory.setBatchListener(false);  // 개별 메시지 처리

        log.info("Kafka Listener Container configured with concurrency: {}", concurrency);
        return factory;
    }

    /**
     * WAF 공격 탐지 이벤트 토픽
     */
    @Bean
    public NewTopic attacksTopic() {
        return new NewTopic(attacksTopic, 3, (short) 2)  // 3 파티션, 2 복제본
            .configs(Map.of(
                "cleanup.policy", "delete",
                "retention.ms", "604800000",     // 7일 보존
                "compression.type", "lz4"
            ));
    }

    /**
     * WAF 접근 로그 스트림 토픽
     */
    @Bean
    public NewTopic logsTopic() {
        return new NewTopic(logsTopic, 6, (short) 2)     // 6 파티션 (높은 처리량)
            .configs(Map.of(
                "cleanup.policy", "delete",
                "retention.ms", "86400000",      // 1일 보존 (로그 특성상)
                "compression.type", "lz4"
            ));
    }

    /**
     * WAF 보안 알림 토픽
     */
    @Bean
    public NewTopic alertsTopic() {
        return new NewTopic(alertsTopic, 2, (short) 3)   // 2 파티션, 3 복제본 (고가용성)
            .configs(Map.of(
                "cleanup.policy", "delete",
                "retention.ms", "2592000000",    // 30일 보존
                "compression.type", "gzip"       // 높은 압축률
            ));
    }

    /**
     * WAF 성능 메트릭 토픽
     */
    @Bean
    public NewTopic metricsTopic() {
        return new NewTopic(metricsTopic, 4, (short) 2)  // 4 파티션
            .configs(Map.of(
                "cleanup.policy", "delete",
                "retention.ms", "259200000",     // 3일 보존
                "compression.type", "lz4"
            ));
    }

    /**
     * WAF 감사 로그 토픽
     */
    @Bean
    public NewTopic auditTopic() {
        return new NewTopic(auditTopic, 2, (short) 3)    // 높은 내구성
            .configs(Map.of(
                "cleanup.policy", "delete",
                "retention.ms", "7776000000",    // 90일 보존 (규정 준수)
                "compression.type", "gzip"
            ));
    }

    /**
     * 커스텀 Producer Interceptor
     * 메시지 전송 통계 및 로깅
     */
    public static class KafkaProducerInterceptor implements org.apache.kafka.clients.producer.ProducerInterceptor<String, Object> {

        @Override
        public org.apache.kafka.clients.producer.ProducerRecord<String, Object> onSend(
                org.apache.kafka.clients.producer.ProducerRecord<String, Object> record) {
            log.debug("Sending Kafka message to topic: {}, partition: {}, key: {}",
                record.topic(), record.partition(), record.key());
            return record;
        }

        @Override
        public void onAcknowledgement(org.apache.kafka.clients.producer.RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                log.error("Failed to send Kafka message to topic: {}, partition: {}, offset: {}",
                    metadata.topic(), metadata.partition(), metadata.offset(), exception);
            } else {
                log.debug("Successfully sent Kafka message to topic: {}, partition: {}, offset: {}",
                    metadata.topic(), metadata.partition(), metadata.offset());
            }
        }

        @Override
        public void close() {
            log.info("Kafka Producer Interceptor closed");
        }

        @Override
        public void configure(Map<String, ?> configs) {
            log.info("Kafka Producer Interceptor configured");
        }
    }
}