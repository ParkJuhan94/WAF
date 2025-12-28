package dev.waf.console.service;

import dev.waf.console.event.AttackDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Slack 알림 서비스
 *
 * 긴급 공격 탐지 시 Slack Webhook으로 알림을 전송합니다.
 * - Block Kit 형식의 Rich Message
 * - Redis 기반 Rate Limiting
 * - 비동기 전송
 *
 * @author WAF Console Team
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SlackNotificationService {

    @Value("${waf.console.slack.enabled:false}")
    private boolean slackEnabled;

    @Value("${waf.console.slack.webhook-url:}")
    private String webhookUrl;

    @Value("${waf.console.slack.rate-limit-minutes:5}")
    private int rateLimitMinutes;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 긴급 공격 알림 전송
     *
     * @param event 공격 탐지 이벤트
     */
    public void sendCriticalAlert(AttackDetectedEvent event) {
        if (!slackEnabled) {
            log.debug("Slack notification disabled");
            return;
        }

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("Slack webhook URL not configured");
            return;
        }

        // Rate Limiting
        String rateLimitKey = "slack:alert:" + event.getSourceIp();
        if (!checkRateLimit(rateLimitKey)) {
            log.debug("Slack rate limit exceeded for IP: {}", event.getSourceIp());
            return;
        }

        // 비동기 전송
        CompletableFuture.runAsync(() -> {
            try {
                String payload = buildSlackPayload(event);
                sendWebhook(payload);
                log.info("Slack alert sent for attack from {}", event.getSourceIp());
            } catch (Exception e) {
                log.error("Failed to send Slack notification", e);
            }
        });
    }

    /**
     * Slack Block Kit 메시지 구성
     *
     * @param event 공격 탐지 이벤트
     * @return JSON 형식의 Slack 메시지
     */
    private String buildSlackPayload(AttackDetectedEvent event) {
        String attackTypeDesc = event.getAttackType() != null
                ? event.getAttackType().name().replace("_", " ")
                : "UNKNOWN";

        String payload = truncate(event.getPayload() != null ? event.getPayload() : "N/A", 200);

        return String.format("""
        {
            "blocks": [
                {
                    "type": "header",
                    "text": {
                        "type": "plain_text",
                        "text": "🚨 WAF Critical Attack Detected",
                        "emoji": true
                    }
                },
                {
                    "type": "section",
                    "fields": [
                        {
                            "type": "mrkdwn",
                            "text": "*Attack Type:*\\n%s"
                        },
                        {
                            "type": "mrkdwn",
                            "text": "*Source IP:*\\n`%s`"
                        },
                        {
                            "type": "mrkdwn",
                            "text": "*Risk Score:*\\n%d/100"
                        },
                        {
                            "type": "mrkdwn",
                            "text": "*Target URL:*\\n%s"
                        },
                        {
                            "type": "mrkdwn",
                            "text": "*Blocked:*\\n%s"
                        },
                        {
                            "type": "mrkdwn",
                            "text": "*Time:*\\n%s"
                        }
                    ]
                },
                {
                    "type": "section",
                    "text": {
                        "type": "mrkdwn",
                        "text": "*Payload:*\\n```%s```"
                    }
                },
                {
                    "type": "divider"
                },
                {
                    "type": "context",
                    "elements": [
                        {
                            "type": "mrkdwn",
                            "text": "Event ID: `%s` | WAF Console Alert System"
                        }
                    ]
                }
            ]
        }
        """,
                attackTypeDesc,
                event.getSourceIp() != null ? event.getSourceIp() : "UNKNOWN",
                event.getRiskScore() != null ? event.getRiskScore() : 0,
                event.getTargetUrl() != null ? event.getTargetUrl() : "UNKNOWN",
                event.getBlocked() != null && event.getBlocked() ? "✅ Yes" : "❌ No",
                event.getTimestamp() != null ? event.getTimestamp().toString() : "UNKNOWN",
                payload,
                event.getEventId() != null ? event.getEventId() : "UNKNOWN"
        );
    }

    /**
     * Rate Limiting 체크
     *
     * @param key Redis 키
     * @return 허용 여부
     */
    private boolean checkRateLimit(String key) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(rateLimitMinutes));
            }
            return count != null && count <= 1;
        } catch (Exception e) {
            log.error("Rate limit check failed", e);
            return true; // Fail open: 에러 시 알림 허용
        }
    }

    /**
     * Slack Webhook 전송
     *
     * @param payload JSON 메시지
     * @throws IOException          HTTP 요청 실패
     * @throws InterruptedException HTTP 요청 중단
     */
    private void sendWebhook(String payload) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Slack webhook failed: HTTP " + response.statusCode()
                    + ", body: " + response.body());
        }
    }

    /**
     * 문자열 truncate
     *
     * @param str       원본 문자열
     * @param maxLength 최대 길이
     * @return truncate된 문자열
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
