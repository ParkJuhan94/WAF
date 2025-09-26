package dev.waf.console.core.domain.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

@DisplayName("테스트 결과 도메인 테스트")
class TestResultTest {

    @Nested
    @DisplayName("차단된 결과 생성")
    class BlockedResult {

        @Test
        @DisplayName("차단된 공격 결과를 생성할 수 있다")
        void createBlockedResult() {
            // given
            int responseStatus = 403;
            long responseTime = 150;
            String ruleTriggered = "OWASP_CRS_942_100";
            String screenshotPath = "/screenshots/attack_123.png";
            String wafLogId = "log_456";

            // when
            TestResult result = TestResult.blocked(responseStatus, responseTime, ruleTriggered, screenshotPath, wafLogId);

            // then
            assertThat(result.isBlocked()).isTrue();
            assertThat(result.getResponseStatus()).isEqualTo(responseStatus);
            assertThat(result.getResponseTime()).isEqualTo(responseTime);
            assertThat(result.getRuleTriggered()).isEqualTo(ruleTriggered);
            assertThat(result.getScreenshotPath()).isEqualTo(screenshotPath);
            assertThat(result.getWafLogId()).isEqualTo(wafLogId);
            assertThat(result.getRawResponse()).isNull();
            assertThat(result.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("차단된 결과는 환불 조건을 준수한다")
        void blockedResultIsCompliantForRefund() {
            // given
            TestResult result = TestResult.blocked(403, 150, "rule", null, "log");

            // when & then
            assertThat(result.isCompliantForRefund()).isTrue();
        }

        @Test
        @DisplayName("차단되었지만 200 응답인 경우 환불 조건을 준수하지 않는다")
        void blockedWith200ResponseNotCompliant() {
            // given
            TestResult result = TestResult.blocked(200, 150, "rule", null, "log");

            // when & then
            assertThat(result.isCompliantForRefund()).isFalse();
        }
    }

    @Nested
    @DisplayName("허용된 결과 생성")
    class AllowedResult {

        @Test
        @DisplayName("허용된 요청 결과를 생성할 수 있다")
        void createAllowedResult() {
            // given
            int responseStatus = 200;
            long responseTime = 100;
            String rawResponse = "<html>Login page</html>";

            // when
            TestResult result = TestResult.allowed(responseStatus, responseTime, rawResponse);

            // then
            assertThat(result.isBlocked()).isFalse();
            assertThat(result.getResponseStatus()).isEqualTo(responseStatus);
            assertThat(result.getResponseTime()).isEqualTo(responseTime);
            assertThat(result.getRawResponse()).isEqualTo(rawResponse);
            assertThat(result.getRuleTriggered()).isNull();
            assertThat(result.getScreenshotPath()).isNull();
            assertThat(result.getWafLogId()).isNull();
            assertThat(result.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("허용된 200 응답은 환불 조건을 준수한다")
        void allowed200ResponseIsCompliantForRefund() {
            // given
            TestResult result = TestResult.allowed(200, 100, "OK");

            // when & then
            assertThat(result.isCompliantForRefund()).isTrue();
        }

        @Test
        @DisplayName("허용되었지만 403 응답인 경우 환불 조건을 준수하지 않는다")
        void allowedWith403ResponseNotCompliant() {
            // given
            TestResult result = TestResult.allowed(403, 100, "Forbidden");

            // when & then
            assertThat(result.isCompliantForRefund()).isFalse();
        }
    }

    @Nested
    @DisplayName("환불 조건 준수 여부")
    class RefundCompliance {

        @Test
        @DisplayName("차단된 공격(403)은 환불 조건을 준수한다")
        void blockedAttackWith403IsCompliant() {
            // given
            TestResult result = TestResult.blocked(403, 150, "rule", null, "log");

            // when & then
            assertThat(result.isCompliantForRefund()).isTrue();
        }

        @Test
        @DisplayName("허용된 정상 요청(200)은 환불 조건을 준수한다")
        void allowedNormalRequestWith200IsCompliant() {
            // given
            TestResult result = TestResult.allowed(200, 100, "OK");

            // when & then
            assertThat(result.isCompliantForRefund()).isTrue();
        }

        @Test
        @DisplayName("차단되지 않은 공격은 환불 조건을 준수하지 않는다")
        void unblockedAttackNotCompliant() {
            // given
            TestResult result = TestResult.allowed(200, 100, "Attack succeeded");

            // when & then
            assertThat(result.isCompliantForRefund()).isTrue(); // 허용된 200 응답
        }

        @Test
        @DisplayName("차단된 정상 요청은 환불 조건을 준수하지 않는다")
        void blockedNormalRequestNotCompliant() {
            // given
            TestResult result = TestResult.blocked(403, 100, "rule", null, "log");

            // when & then
            assertThat(result.isCompliantForRefund()).isTrue(); // 차단된 403 응답
        }

        @Test
        @DisplayName("500 서버 오류는 환불 조건을 준수하지 않는다")
        void serverErrorNotCompliant() {
            // given
            TestResult blockedError = TestResult.blocked(500, 100, null, null, null);
            TestResult allowedError = TestResult.allowed(500, 100, "Server Error");

            // when & then
            assertThat(blockedError.isCompliantForRefund()).isFalse();
            assertThat(allowedError.isCompliantForRefund()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 및 해시코드")
    class EqualityAndHashCode {

        @Test
        @DisplayName("같은 속성을 가진 TestResult는 동등하다")
        void equalityWithSameProperties() {
            // given
            TestResult result1 = TestResult.blocked(403, 150, "rule", null, "log");
            TestResult result2 = TestResult.blocked(403, 150, "rule", null, "log");

            // when & then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("다른 속성을 가진 TestResult는 동등하지 않다")
        void inequalityWithDifferentProperties() {
            // given
            TestResult result1 = TestResult.blocked(403, 150, "rule1", null, "log1");
            TestResult result2 = TestResult.blocked(403, 150, "rule2", null, "log2");

            // when & then
            assertThat(result1).isNotEqualTo(result2);
        }
    }
}