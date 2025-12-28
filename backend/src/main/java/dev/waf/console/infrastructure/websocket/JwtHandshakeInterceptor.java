package dev.waf.console.infrastructure.websocket;

import dev.waf.console.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Handshake JWT 인증 인터셉터
 *
 * WebSocket 연결 수립 전에 JWT 토큰을 검증합니다.
 * - Query parameter에서 토큰 추출 (/ws?token=JWT_TOKEN)
 * - 유효한 토큰인 경우에만 연결 허용
 * - userId를 WebSocket 세션 속성에 저장
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Handshake 전에 JWT 토큰 검증
     *
     * @param request    HTTP 요청
     * @param response   HTTP 응답
     * @param wsHandler  WebSocket 핸들러
     * @param attributes 세션 속성
     * @return 연결 허용 여부
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        try {
            // Query parameter에서 토큰 추출
            String token = extractTokenFromQuery(request);

            if (token == null) {
                log.warn("WebSocket connection attempt without token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // 토큰 검증
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket connection attempt with invalid token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // userId 추출 및 세션 속성에 저장
            String userId = jwtTokenProvider.getUserId(token);
            attributes.put("userId", userId);

            log.info("WebSocket connection authorized for user: {}", userId);
            return true;

        } catch (Exception e) {
            log.error("Error during WebSocket handshake authentication", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    /**
     * Handshake 후 처리 (필요 시 구현)
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                              ServerHttpResponse response,
                              WebSocketHandler wsHandler,
                              Exception exception) {
        if (exception != null) {
            log.error("Error after WebSocket handshake", exception);
        }
    }

    /**
     * Query parameter에서 JWT 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 또는 null
     */
    private String extractTokenFromQuery(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            return servletRequest.getServletRequest().getParameter("token");
        }

        // Fallback: URI에서 직접 파싱
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring("token=".length());
                }
            }
        }

        return null;
    }
}
