package dev.waf.console.core.domain.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

@DisplayName("공격 시뮬레이션 도메인 테스트")
class AttackSimulationTest {

    @Nested
    @DisplayName("공격 시뮬레이션 생성")
    class CreateSimulation {

        @Test
        @DisplayName("환불 조건 검증용 시뮬레이션을 생성할 수 있다")
        void createRefundValidationSimulation() {
            // given
            String targetUrl = "https://test-waf.example.com";
            AttackType attackType = AttackType.SQL_INJECTION;
            String executedBy = "test-user";

            // when
            AttackSimulation simulation = AttackSimulation.forRefundValidation(targetUrl, attackType, executedBy);

            // then
            assertThat(simulation.getId()).isNotNull();
            assertThat(simulation.getTargetUrl()).isEqualTo(targetUrl);
            assertThat(simulation.getAttackType()).isEqualTo(attackType);
            assertThat(simulation.getExecutedBy()).isEqualTo(executedBy);
            assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.PENDING);
            assertThat(simulation.getTestName()).contains("환불 조건 검증");
            assertThat(simulation.getTestName()).contains(attackType.getDisplayName());
            assertThat(simulation.getPayload()).isEqualTo(attackType.getDefaultPayload());
        }

        @Test
        @DisplayName("DVWA 정상 요청 테스트용 시뮬레이션을 생성할 수 있다")
        void createDvwaNormalTestSimulation() {
            // given
            String dvwaUrl = "https://dvwa.example.com";
            String path = "/login.php";
            String executedBy = "test-user";

            // when
            AttackSimulation simulation = AttackSimulation.forDvwaNormalTest(dvwaUrl, path, executedBy);

            // then
            assertThat(simulation.getTargetUrl()).isEqualTo(dvwaUrl + path);
            assertThat(simulation.getAttackType()).isEqualTo(AttackType.DVWA_NORMAL);
            assertThat(simulation.getPayload()).isEmpty();
            assertThat(simulation.getTestName()).contains("DVWA 정상 요청 테스트");
        }

        @Test
        @DisplayName("필수 파라미터가 null이면 예외가 발생한다")
        void throwExceptionWhenRequiredParametersAreNull() {
            // given
            SimulationId id = SimulationId.generate();
            String validUrl = "https://test.com";
            AttackType validType = AttackType.XSS;

            // when & then
            assertThatThrownBy(() -> new AttackSimulation(null, "test", validUrl, validType, "", "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SimulationId cannot be null");

            assertThatThrownBy(() -> new AttackSimulation(id, "test", null, validType, "", "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target URL cannot be null or empty");

            assertThatThrownBy(() -> new AttackSimulation(id, "test", "", validType, "", "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Target URL cannot be null or empty");

            assertThatThrownBy(() -> new AttackSimulation(id, "test", validUrl, null, "", "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AttackType cannot be null");
        }
    }

    @Nested
    @DisplayName("시뮬레이션 실행")
    class ExecuteSimulation {

        @Test
        @DisplayName("PENDING 상태에서 시뮬레이션을 시작할 수 있다")
        void startSimulationFromPendingState() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");

            // when
            simulation.start();

            // then
            assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.RUNNING);
        }

        @Test
        @DisplayName("PENDING 상태가 아니면 시작할 수 없다")
        void cannotStartSimulationFromNonPendingState() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");
            simulation.start();

            // when & then
            assertThatThrownBy(simulation::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot start simulation in RUNNING state");
        }

        @Test
        @DisplayName("RUNNING 상태에서 시뮬레이션을 완료할 수 있다")
        void completeSimulationFromRunningState() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");
            simulation.start();
            TestResult result = TestResult.blocked(403, 150, "OWASP_CRS_942_100", null, "log123");

            // when
            simulation.complete(result);

            // then
            assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.COMPLETED);
            assertThat(simulation.getResult()).isEqualTo(result);
            assertThat(simulation.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("RUNNING 상태가 아니면 완료할 수 없다")
        void cannotCompleteSimulationFromNonRunningState() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");
            TestResult result = TestResult.blocked(403, 150, "rule", null, "log123");

            // when & then
            assertThatThrownBy(() -> simulation.complete(result))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot complete simulation in PENDING state");
        }

        @Test
        @DisplayName("null 결과로는 완료할 수 없다")
        void cannotCompleteSimulationWithNullResult() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");
            simulation.start();

            // when & then
            assertThatThrownBy(() -> simulation.complete(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TestResult cannot be null");
        }

        @Test
        @DisplayName("시뮬레이션을 실패로 처리할 수 있다")
        void failSimulation() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");
            simulation.start();

            // when
            simulation.fail("Network timeout");

            // then
            assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.FAILED);
            assertThat(simulation.getCompletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("환불 조건 준수 여부")
    class RefundCompliance {

        @Test
        @DisplayName("공격이 차단되면 환불 조건을 준수한다")
        void compliantWhenAttackIsBlocked() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");
            simulation.start();
            TestResult blockedResult = TestResult.blocked(403, 150, "OWASP_CRS_942_100", null, "log123");

            // when
            simulation.complete(blockedResult);

            // then
            assertThat(simulation.isCompliantForRefund()).isTrue();
        }

        @Test
        @DisplayName("정상 요청이 허용되면 환불 조건을 준수한다")
        void compliantWhenNormalRequestIsAllowed() {
            // given
            AttackSimulation simulation = AttackSimulation.forDvwaNormalTest(
                "https://dvwa.com", "/login.php", "user");
            simulation.start();
            TestResult allowedResult = TestResult.allowed(200, 100, "OK");

            // when
            simulation.complete(allowedResult);

            // then
            assertThat(simulation.isCompliantForRefund()).isTrue();
        }

        @Test
        @DisplayName("완료되지 않은 시뮬레이션은 환불 조건을 준수하지 않는다")
        void notCompliantWhenNotCompleted() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");

            // when & then
            assertThat(simulation.isCompliantForRefund()).isFalse();
        }

        @Test
        @DisplayName("결과가 없는 시뮬레이션은 환불 조건을 준수하지 않는다")
        void notCompliantWhenNoResult() {
            // given
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                "https://test.com", AttackType.SQL_INJECTION, "user");
            simulation.start();
            simulation.fail("Test failed");

            // when & then
            assertThat(simulation.isCompliantForRefund()).isFalse();
        }
    }
}