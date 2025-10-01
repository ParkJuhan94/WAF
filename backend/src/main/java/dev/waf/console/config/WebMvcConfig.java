package dev.waf.console.config;

import dev.waf.console.infrastructure.logging.WAFLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 *
 * Spring MVC 관련 설정을 담당
 * - CORS 설정
 * - 인터셉터 등록
 * - 정적 리소스 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final WAFLoggingInterceptor wafLoggingInterceptor;

    /**
     * 인터셉터 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(wafLoggingInterceptor)
            .addPathPatterns("/api/**")  // API 요청에만 적용
            .excludePathPatterns(
                "/api/health",           // 헬스체크 제외
                "/api/actuator/**",      // Actuator 엔드포인트 제외
                "/api-docs/**",          // Swagger 문서 제외
                "/swagger-ui/**"         // Swagger UI 제외
            );

        log.info("WAF logging interceptor registered for /api/** paths");
    }

    /**
     * CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);

        log.info("CORS configured for /api/** paths");
    }
}