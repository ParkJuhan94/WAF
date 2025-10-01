package dev.waf.console.config;

import org.testcontainers.containers.GenericContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * TestContainers 통합 테스트 설정
 *
 * 실제 환경과 동일한 인프라 컨테이너 제공:
 * - MySQL: 데이터베이스 테스트
 * - Redis: 캐시 및 세션 테스트
 *
 * 컨테이너 간 네트워크 격리 및 성능 최적화 적용
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

    /**
     * 컨테이너 간 통신을 위한 공통 네트워크
     */
    @Bean
    public Network testNetwork() {
        return Network.newNetwork();
    }

    /**
     * MySQL 테스트 컨테이너
     *
     * 특징:
     * - 실제 MySQL 8.0 사용으로 운영 환경과 동일
     * - 트랜잭션, 제약조건, 인덱스 등 모든 기능 테스트 가능
     * - 자동 데이터베이스 초기화
     */
    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer(Network network) {
        MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("waf_test")
                .withUsername("test_user")
                .withPassword("test_password")
                .withNetwork(network)
                .withNetworkAliases("mysql-test")
                .withReuse(true)  // 컨테이너 재사용으로 테스트 속도 향상
                .withCommand(
                    "--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_unicode_ci",
                    "--innodb-flush-log-at-trx-commit=0",  // 테스트 성능 향상
                    "--sync-binlog=0",                     // 테스트 성능 향상
                    "--innodb-buffer-pool-size=256M"       // 메모리 사용량 최적화
                );

        mysql.start();

        System.out.println("MySQL TestContainer started: " + mysql.getHost() + ":" + mysql.getMappedPort(MySQLContainer.MYSQL_PORT));

        return mysql;
    }

    /**
     * Redis 테스트 컨테이너
     *
     * 특징:
     * - 실제 Redis 7.0 사용
     * - 캐시, 세션, 분산 잠금 등 모든 Redis 기능 테스트
     * - 메모리 제한으로 테스트 환경 최적화
     */
    @Bean
    @ServiceConnection
    public GenericContainer<?> redisContainer(Network network) {
        GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
                .withExposedPorts(6379)
                .withNetwork(network)
                .withNetworkAliases("redis-test")
                .withReuse(true)  // 컨테이너 재사용
                .withCommand("redis-server",
                    "--maxmemory", "128mb",              // 메모리 제한
                    "--maxmemory-policy", "allkeys-lru", // LRU 정책
                    "--save", "",                        // 지속성 비활성화 (테스트용)
                    "--appendonly", "no"                 // AOF 비활성화 (테스트용)
                );

        redis.start();

        System.out.println("Redis TestContainer started: " + redis.getHost() + ":" + redis.getMappedPort(6379));

        return redis;
    }

    /**
     * 동적 프로퍼티 설정
     * 테스트 실행시 컨테이너 연결 정보를 자동 주입
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry,
                                  MySQLContainer<?> mysql,
                                  GenericContainer<?> redis) {

        // MySQL 설정
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);

        // Redis 설정
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
        registry.add("spring.data.redis.database", () -> "0");

        // JPA 테스트 설정
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");

        // 로깅 레벨 조정
        registry.add("logging.level.org.testcontainers", () -> "INFO");
        registry.add("logging.level.dev.waf.console", () -> "DEBUG");

        System.out.println("TestContainers dynamic properties configured");
    }

    /**
     * 테스트 컨테이너 헬스 체크
     */
    @Bean
    public TestContainerHealthCheck testContainerHealthCheck(
            MySQLContainer<?> mysql,
            GenericContainer<?> redis) {
        return new TestContainerHealthCheck(mysql, redis);
    }

    /**
     * 테스트 컨테이너 상태 확인 유틸리티
     */
    public static class TestContainerHealthCheck {
        private final MySQLContainer<?> mysql;
        private final GenericContainer<?> redis;

        public TestContainerHealthCheck(MySQLContainer<?> mysql, GenericContainer<?> redis) {
            this.mysql = mysql;
            this.redis = redis;
        }

        public boolean areContainersHealthy() {
            boolean mysqlHealthy = mysql.isRunning() && mysql.isHealthy();
            boolean redisHealthy = redis.isRunning() && redis.isHealthy();

            if (!mysqlHealthy) {
                System.err.println("MySQL container is not healthy");
            }
            if (!redisHealthy) {
                System.err.println("Redis container is not healthy");
            }

            return mysqlHealthy && redisHealthy;
        }

        public void logContainerInfo() {
            System.out.println("=== TestContainer Information ===");
            System.out.println("MySQL: " + mysql.getHost() + ":" + mysql.getMappedPort(MySQLContainer.MYSQL_PORT) + " (Database: " + mysql.getDatabaseName() + ")");
            System.out.println("Redis: " + redis.getHost() + ":" + redis.getMappedPort(6379));
            System.out.println("================================");
        }
    }
}