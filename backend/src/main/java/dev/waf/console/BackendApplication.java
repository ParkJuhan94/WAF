package dev.waf.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "dev.waf.console.user.repository",
    "dev.waf.console.customrule.repository",
    "dev.waf.console.waflog.repository"
})
@EntityScan(basePackages = {
    "dev.waf.console.user.domain",
    "dev.waf.console.customrule.domain",
    "dev.waf.console.waflog.domain"
})
@EnableJpaAuditing
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
