package dev.waf.console.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Repository 설정
 */
@Configuration
@EnableJpaRepositories(basePackages = "dev.waf.console.infrastructure.persistence.repository")
@EntityScan(basePackages = "dev.waf.console.core.domain")
public class RepositoryConfig {
}