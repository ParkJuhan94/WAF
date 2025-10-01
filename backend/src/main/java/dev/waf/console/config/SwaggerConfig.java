package dev.waf.console.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3.0 설정
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.api.url:http://localhost:8080}")
    private String apiUrl;

    /**
     * OpenAPI 3.0 설정
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server().url(apiUrl).description("Development Server"),
                    new Server().url("https://api.waf-console.dev").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("JWT", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.")
                    )
                );
    }

    /**
     * API 정보 설정
     *
     * @return API 정보 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("🛡️ WAF Console API")
                .description("""
                    ## WAF Console Backend API Documentation

                    **ModSecurity + OWASP CRS 기반 웹 애플리케이션 방화벽 관리 SaaS 플랫폼**

                    ### 🔥 주요 기능
                    - 🔐 **Google OAuth 2.0 인증** - 소셜 로그인 기반 사용자 관리
                    - 📊 **실시간 대시보드** - WebSocket 기반 실시간 트래픽 모니터링
                    - 🛡️ **WAF 룰 관리** - ModSecurity 룰 생성/편집/배포
                    - 🎯 **공격 시뮬레이션** - 5가지 공격 유형 차단 테스트
                    - 📈 **고급 분석** - 공격 패턴 분석 및 위험도 평가                   

                    ### 🏗️ 아키텍처
                    - **Clean Architecture** + **CQRS** + **Event-Driven Architecture**
                    - **Spring Boot 3.x** + **Java 21** + **MySQL** + **Redis** + **Elasticsearch**
                    - **JWT 인증** + **OAuth2** + **멀티테넌트 SaaS**

                    ### 🔑 인증 방법
                    1. `/api/v1/auth/google` 엔드포인트로 Google OAuth 로그인
                    2. 응답받은 JWT 토큰을 복사
                    3. 우측 상단 **Authorize** 버튼 클릭
                    4. `Bearer {token}` 형식으로 입력 (Bearer 자동 추가됨)

                    ### 📱 프론트엔드
                    React + TypeScript + Ant Design 기반 SaaS 콘솔
                    """)
                .version(appVersion)
                .contact(new Contact()
                    .name("WAF Console Team")
                    .email("support@waf-console.dev")
                    .url("https://github.com/waf-console/backend")
                )
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
                );
    }
}