package dev.waf.console.config;

import dev.waf.console.infrastructure.security.JwtTokenProvider;
import dev.waf.console.infrastructure.websocket.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket 설정
 *
 * STOMP 프로토콜을 사용한 양방향 통신 설정
 * - 실시간 대시보드 업데이트
 * - 공격 이벤트 브로드캐스트
 * - JWT 기반 인증
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * TaskScheduler for WebSocket heartbeat
     */
    @Bean
    public TaskScheduler heartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    /**
     * 메시지 브로커 설정
     * - /topic: 브로드캐스트용 (1:N)
     * - /queue: 개인 메시지용 (1:1)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory message broker
        registry.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000}) // 10초 heartbeat
                .setTaskScheduler(heartbeatScheduler());

        // Application destination prefix
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 등록
     * - /ws: WebSocket 연결 엔드포인트
     * - SockJS fallback 지원
     * - JWT 인증 인터셉터 추가
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:3000")
                .addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider))
                .withSockJS();
    }

    /**
     * WebSocket transport 설정
     * - 메시지 크기 제한
     * - 전송 버퍼 크기
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024);      // 128KB
        registration.setSendBufferSizeLimit(512 * 1024);   // 512KB
        registration.setSendTimeLimit(20 * 1000);          // 20초
    }
}
