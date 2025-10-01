package dev.waf.console.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 캐시 설정
 *
 * 확장 가능한 멀티레벨 캐시 아키텍처를 위한 Redis 설정
 * - L2 캐시로 Redis 사용 (L1은 Caffeine)
 * - 캐시별 TTL 세밀 제어
 * - JSON 직렬화로 가독성 향상
 * - 타입 안전성 보장
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private final RedisProperties redisProperties;

    /**
     * Redis JSON 직렬화용 ObjectMapper
     * 타입 정보 포함하여 역직렬화시 타입 안전성 보장
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    /**
     * Redis JSON 직렬화 설정
     */
    @Bean
    public GenericJackson2JsonRedisSerializer redisJsonSerializer() {
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper());
    }

    /**
     * 범용 RedisTemplate 설정
     * String 키와 JSON 값 직렬화
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer jsonSerializer) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 직렬화: String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value 직렬화: JSON
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();

        log.info("RedisTemplate configured with JSON serialization");
        return template;
    }

    /**
     * 캐시별 TTL 세밀 제어를 위한 RedisCacheManager
     *
     * 캐시 네임별로 다른 TTL 적용:
     * - jwt-blacklist: 1시간 (토큰 만료시간과 동일)
     * - user-profile: 30분 (사용자 정보 캐싱)
     * - api-response: 5분 (API 응답 캐싱)
     * - stats-cache: 1분 (실시간 통계)
     * - rule-cache: 10분 (WAF 룰 캐싱)
     */
    @Bean
    @Primary
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer jsonSerializer) {

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(redisProperties.getCache().getDefaultTtlMinutes()))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // 캐시별 개별 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // JWT 블랙리스트 (1시간)
        cacheConfigurations.put("jwt-blacklist",
            defaultConfig.entryTtl(Duration.ofHours(1)));

        // 사용자 프로필 (30분)
        cacheConfigurations.put("user-profile",
            defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // API 응답 캐시 (5분)
        cacheConfigurations.put("api-response",
            defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 실시간 통계 (1분)
        cacheConfigurations.put("stats-cache",
            defaultConfig.entryTtl(Duration.ofMinutes(1)));

        // WAF 룰 캐시 (10분)
        cacheConfigurations.put("rule-cache",
            defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // 로그 검색 결과 (3분)
        cacheConfigurations.put("log-search",
            defaultConfig.entryTtl(Duration.ofMinutes(3)));

        // 화이트리스트 (15분)
        cacheConfigurations.put("whitelist-cache",
            defaultConfig.entryTtl(Duration.ofMinutes(15)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();

        log.info("RedisCacheManager configured with {} cache configurations",
            cacheConfigurations.size());

        return cacheManager;
    }

    /**
     * Redis 연결 상태 확인용 빈
     */
    @Bean
    public RedisHealthIndicator redisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        return new RedisHealthIndicator(redisTemplate);
    }

    /**
     * Redis 연결 상태 체크
     */
    public static class RedisHealthIndicator {
        private final RedisTemplate<String, Object> redisTemplate;

        public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        public boolean isHealthy() {
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                return true;
            } catch (Exception e) {
                log.warn("Redis health check failed", e);
                return false;
            }
        }
    }
}