package dev.waf.console.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * 통합 테스트를 위한 추상 베이스 클래스
 *
 * 모든 통합 테스트에서 공통으로 사용하는 설정과 유틸리티 제공:
 * - TestContainers 자동 설정 (MySQL, Redis)
 * - 트랜잭션 롤백으로 테스트 격리
 * - 캐시 초기화
 * - 공통 테스트 유틸리티
 *
 * 사용법:
 * ```java
 * class MyIntegrationTest extends AbstractIntegrationTest {
 *     @Test
 *     void testSomething() {
 *         // 테스트 코드
 *     }
 * }
 * ```
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@Transactional  // 각 테스트 후 자동 롤백
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @Autowired
    protected CacheManager cacheManager;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private TestContainersConfig.TestContainerHealthCheck healthCheck;

    /**
     * 각 테스트 실행 전 공통 설정
     */
    @BeforeEach
    void setUpIntegrationTest() {
        // 컨테이너 상태 확인
        if (!healthCheck.areContainersHealthy()) {
            throw new IllegalStateException("Test containers are not healthy");
        }

        // 캐시 전체 초기화 (테스트 격리)
        clearAllCaches();

        // Redis 데이터 초기화 (테스트 격리)
        clearRedisData();

        // JPA 1차 캐시 초기화
        entityManager.clear();
    }

    /**
     * 모든 캐시 초기화
     */
    protected void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    /**
     * Redis 데이터 초기화
     */
    protected void clearRedisData() {
        var connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushDb();
        connection.close();
    }

    /**
     * 엔티티 강제 플러시 및 초기화
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * JSON 문자열로 변환
     */
    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("JSON 변환 실패", e);
        }
    }

    /**
     * JSON에서 객체로 변환
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    /**
     * Redis에 테스트 데이터 저장
     */
    protected void setRedisData(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Redis에서 테스트 데이터 조회
     */
    protected Object getRedisData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Redis 키 존재 여부 확인
     */
    protected boolean existsRedisKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 캐시 데이터 존재 여부 확인
     */
    protected boolean existsCacheData(String cacheName, String key) {
        var cache = cacheManager.getCache(cacheName);
        return cache != null && cache.get(key) != null;
    }

    /**
     * 캐시에서 데이터 조회
     */
    protected Object getCacheData(String cacheName, String key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            var wrapper = cache.get(key);
            return wrapper != null ? wrapper.get() : null;
        }
        return null;
    }

    /**
     * 테스트 데이터 생성 헬퍼 메서드들
     * 하위 클래스에서 override하여 도메인별 테스트 데이터 생성
     */

    /**
     * 테스트용 사용자 생성
     */
    protected Object createTestUser(String email, String name) {
        // 하위 클래스에서 구현
        return null;
    }

    /**
     * 테스트용 WAF 룰 생성
     */
    protected Object createTestRule(String ruleName, String content) {
        // 하위 클래스에서 구현
        return null;
    }

    /**
     * 테스트용 로그 엔트리 생성
     */
    protected Object createTestLog(String clientIp, String attackType) {
        // 하위 클래스에서 구현
        return null;
    }

    /**
     * 테스트 유틸리티: 지정된 시간 대기
     */
    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("대기 중 인터럽트 발생", e);
        }
    }
}