package dev.waf.console.service;

import dev.waf.console.event.AccessLogEvent;
import dev.waf.console.event.AttackDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 브로드캐스트 서비스
 *
 * Kafka 이벤트를 WebSocket을 통해 실시간으로 브로드캐스트합니다.
 * - 공격 이벤트 → 대시보드 실시간 업데이트
 * - 트래픽 데이터 → 차트 실시간 업데이트
 * - 통계/상태 변경 → UI 실시간 반영
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 공격 이벤트 브로드캐스트
     *
     * @param event 공격 탐지 이벤트
     */
    public void broadcastAttackEvent(AttackDetectedEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/dashboard/attacks", event);
            log.debug("Attack event broadcasted: eventId={}, type={}, sourceIp={}",
                    event.getEventId(), event.getAttackType(), event.getSourceIp());
        } catch (Exception e) {
            log.error("Failed to broadcast attack event: {}", event.getEventId(), e);
        }
    }

    /**
     * 트래픽 업데이트 브로드캐스트
     *
     * @param event 접근 로그 이벤트
     */
    public void broadcastTrafficUpdate(AccessLogEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/dashboard/traffic", event);
            log.trace("Traffic update broadcasted: eventId={}, clientIp={}",
                    event.getEventId(), event.getClientIp());
        } catch (Exception e) {
            log.error("Failed to broadcast traffic update: {}", event.getEventId(), e);
        }
    }

    /**
     * 통계 업데이트 브로드캐스트
     *
     * @param stats 통계 데이터
     */
    public void broadcastStatsUpdate(Object stats) {
        try {
            messagingTemplate.convertAndSend("/topic/dashboard/stats", stats);
            log.debug("Stats update broadcasted");
        } catch (Exception e) {
            log.error("Failed to broadcast stats update", e);
        }
    }

    /**
     * 상태 변경 브로드캐스트
     *
     * @param status 상태 데이터
     */
    public void broadcastStatusChange(Object status) {
        try {
            messagingTemplate.convertAndSend("/topic/dashboard/status", status);
            log.debug("Status change broadcasted");
        } catch (Exception e) {
            log.error("Failed to broadcast status change", e);
        }
    }

    /**
     * 특정 사용자에게 개인 메시지 전송
     *
     * @param userId  사용자 ID
     * @param message 메시지
     */
    public void sendToUser(Long userId, Object message) {
        try {
            messagingTemplate.convertAndSend("/queue/user-" + userId, message);
            log.debug("Personal message sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send message to user: {}", userId, e);
        }
    }
}
