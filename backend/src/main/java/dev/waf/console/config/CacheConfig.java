package dev.waf.console.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.time.Duration;
import java.util.List;

/**
 * 멀티레벨 캐시 설정
 *
 * L1 Cache: Caffeine (애플리케이션 내 메모리)
 * L2 Cache: Redis (분산 캐시)
 *
 * 캐시 계층 구조:
 * 1. 첫 번째 조회: L1 (Caffeine) - 가장 빠름
 * 2. L1 미스시: L2 (Redis) - 네트워크 비용 있지만 분산 환경 지원
 * 3. L2 미스시: 원본 데이터 소스
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    /**
     * L1 캐시: Caffeine (로컬 메모리)
     *
     * 특징:
     * - 매우 빠른 접근 속도
     * - 메모리 제한 있음 (애플리케이션 힙 메모리 사용)
     * - 단일 인스턴스에서만 유효
     * - 자주 접근하는 데이터에 최적
     */
    @Bean("caffeineCacheManager")
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Caffeine 캐시 설정
        Caffeine<Object, Object> caffeineConfig = Caffeine.newBuilder()
                .maximumSize(10_000)              // 최대 10,000개 엔트리
                .expireAfterWrite(Duration.ofMinutes(5))  // 5분 후 만료
                .expireAfterAccess(Duration.ofMinutes(2)) // 2분 미접근시 만료
                .recordStats()                    // 통계 수집 활성화
                .removalListener((key, value, cause) ->
                    log.debug("Caffeine cache evicted: key={}, cause={}", key, cause));

        cacheManager.setCaffeine(caffeineConfig);

        // L1 캐시 대상 캐시명 설정
        cacheManager.setCacheNames(List.of(
            "user-profile-l1",     // 사용자 프로필 (자주 조회)
            "rule-cache-l1",       // WAF 룰 (자주 조회)
            "stats-cache-l1",      // 실시간 통계 (매우 자주 조회)
            "api-response-l1"      // API 응답 (자주 조회)
        ));

        log.info("Caffeine L1 cache manager configured with {} cache names",
            cacheManager.getCacheNames().size());

        return cacheManager;
    }

    /**
     * 복합 캐시 매니저
     *
     * L1 (Caffeine) → L2 (Redis) 순서로 캐시 조회
     * 캐시 미스시 다음 레벨로 폴백
     */
    @Bean("compositeCacheManager")
    public CompositeCacheManager compositeCacheManager(
            CaffeineCacheManager caffeineCacheManager,
            RedisCacheManager redisCacheManager) {

        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();

        // 캐시 매니저 우선순위: L1 → L2
        compositeCacheManager.setCacheManagers(List.of(
            caffeineCacheManager,   // L1: 로컬 메모리 (최우선)
            redisCacheManager       // L2: 분산 캐시
        ));

        // 캐시 미스시 다음 캐시 매니저로 폴백
        compositeCacheManager.setFallbackToNoOpCache(false);

        log.info("Composite cache manager configured with L1(Caffeine) + L2(Redis)");
        return compositeCacheManager;
    }

    /**
     * 캐시 통계 및 모니터링을 위한 빈
     */
    @Bean
    public CacheStatsService cacheStatsService(
            CaffeineCacheManager caffeineCacheManager,
            RedisCacheManager redisCacheManager) {
        return new CacheStatsService(caffeineCacheManager, redisCacheManager);
    }

    /**
     * 캐시 통계 서비스
     * 운영 모니터링을 위한 캐시 통계 정보 제공
     */
    public static class CacheStatsService {
        private final CaffeineCacheManager caffeineCacheManager;
        private final RedisCacheManager redisCacheManager;

        public CacheStatsService(CaffeineCacheManager caffeineCacheManager,
                               RedisCacheManager redisCacheManager) {
            this.caffeineCacheManager = caffeineCacheManager;
            this.redisCacheManager = redisCacheManager;
        }

        /**
         * L1 캐시 통계 조회
         */
        public void logL1CacheStats() {
            caffeineCacheManager.getCacheNames().forEach(cacheName -> {
                var cache = caffeineCacheManager.getCache(cacheName);
                if (cache != null) {
                    var nativeCache = cache.getNativeCache();
                    if (nativeCache instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache) {
                        var stats = caffeineCache.stats();
                        log.info("L1 Cache [{}] - Hit Rate: {:.2f}%, Evictions: {}, Size: {}",
                            cacheName,
                            stats.hitRate() * 100,
                            stats.evictionCount(),
                            caffeineCache.estimatedSize());
                    }
                }
            });
        }

        /**
         * 캐시 전체 초기화 (관리자 기능)
         */
        public void evictAllCaches() {
            log.warn("Evicting all caches (L1 + L2)");

            // L1 캐시 초기화
            caffeineCacheManager.getCacheNames().forEach(cacheName -> {
                var cache = caffeineCacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });

            // L2 캐시 초기화
            redisCacheManager.getCacheNames().forEach(cacheName -> {
                var cache = redisCacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }
}