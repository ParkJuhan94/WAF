package dev.waf.console.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Redis 관련 설정 프로퍼티
 *
 * application.yml의 설정을 타입 안전하게 바인딩
 * 유지보수성과 확장성을 위한 외부 설정 관리
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.redis")
public class RedisProperties {

    /**
     * 캐시 관련 설정
     */
    @Valid
    @NotNull
    private Cache cache = new Cache();

    /**
     * 세션 관리 설정
     */
    @Valid
    @NotNull
    private Session session = new Session();

    /**
     * 캐시 설정
     */
    @Data
    public static class Cache {

        /**
         * 기본 캐시 TTL (분)
         */
        @Min(1)
        private int defaultTtlMinutes = 10;

        /**
         * 캐시 키 접두사
         */
        @NotNull
        private String keyPrefix = "waf:cache:";

        /**
         * 캐시 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 캐시 통계 수집 여부
         */
        private boolean enableStatistics = true;
    }

    /**
     * 세션 관리 설정
     */
    @Data
    public static class Session {

        /**
         * 세션 TTL (분)
         */
        @Min(1)
        private int ttlMinutes = 30;

        /**
         * 세션 키 접두사
         */
        @NotNull
        private String keyPrefix = "waf:session:";

        /**
         * 세션 정리 활성화 여부
         */
        private boolean enableCleanup = true;
    }
}