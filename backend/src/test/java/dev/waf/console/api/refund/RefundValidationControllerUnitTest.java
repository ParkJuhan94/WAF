package dev.waf.console.api.refund;

import dev.waf.console.api.refund.dto.RefundValidationRequest;
import dev.waf.console.api.refund.dto.RefundValidationResponse;
import dev.waf.console.core.domain.simulation.AttackType;
import dev.waf.console.core.repository.JpaSimulationRepository;
import dev.waf.console.core.repository.SimulationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("환불 검증 컨트롤러 단위 테스트")
class RefundValidationControllerUnitTest {

    @Mock
    private JpaSimulationRepository simulationRepository;

    @Mock
    private RefundValidationService refundValidationService;

    private RefundValidationController controller;

    @BeforeEach
    void setUp() {
        controller = new RefundValidationController(simulationRepository, refundValidationService);
    }

    @Test
    @DisplayName("유효한 요청으로 환불 조건 검증을 시작할 수 있다")
    void startValidationWithValidRequest() {
        // given
        RefundValidationRequest request = createValidRequest();
        when(simulationRepository.save(any())).thenReturn(null);

        // when
        ResponseEntity<RefundValidationResponse> response = controller.validateRefundConditions(request, "Bearer test-token");

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(202);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBatchId()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(RefundValidationResponse.ValidationStatus.RUNNING);
        assertThat(response.getBody().getRefundEligibility()).isEqualTo(RefundValidationResponse.RefundEligibility.PENDING);
        assertThat(response.getBody().getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("존재하는 배치 ID로 검증 결과를 조회할 수 있다")
    void getValidationResultWithExistingBatchId() {
        // given
        String batchId = "test-batch-123";
        RefundValidationResponse mockResponse = createCompletedResponse(batchId);
        when(refundValidationService.getValidationResult(batchId)).thenReturn(mockResponse);

        // when
        ResponseEntity<RefundValidationResponse> response = controller.getValidationResult(batchId);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBatchId()).isEqualTo(batchId);
        assertThat(response.getBody().getStatus()).isEqualTo(RefundValidationResponse.ValidationStatus.COMPLETED);
        assertThat(response.getBody().getRefundEligibility()).isEqualTo(RefundValidationResponse.RefundEligibility.ELIGIBLE);
    }

    @Test
    @DisplayName("존재하지 않는 배치 ID로 조회하면 404 오류가 발생한다")
    void returnNotFoundWhenBatchIdDoesNotExist() {
        // given
        String nonExistentBatchId = "non-existent-batch";
        when(refundValidationService.getValidationResult(nonExistentBatchId)).thenReturn(null);

        // when
        ResponseEntity<RefundValidationResponse> response = controller.getValidationResult(nonExistentBatchId);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNull();
    }

    private RefundValidationRequest createValidRequest() {
        RefundValidationRequest request = new RefundValidationRequest();
        request.setTargetUrl("https://test-waf.example.com");
        request.setDvwaUrl("https://dvwa.example.com");
        request.setIncludeScreenshots(true);
        request.setGeneratePdfReport(true);

        Map<AttackType, RefundValidationRequest.TestScenario> attackScenarios = new HashMap<>();
        for (AttackType attackType : AttackType.values()) {
            if (attackType.isRefundCritical()) {
                RefundValidationRequest.TestScenario scenario = new RefundValidationRequest.TestScenario();
                scenario.setEnabled(true);
                scenario.setCustomPayload(attackType.getDefaultPayload());
                attackScenarios.put(attackType, scenario);
            }
        }
        request.setAttackScenarios(attackScenarios);

        return request;
    }

    private RefundValidationResponse createCompletedResponse(String batchId) {
        RefundValidationResponse response = new RefundValidationResponse();
        response.setBatchId(batchId);
        response.setStatus(RefundValidationResponse.ValidationStatus.COMPLETED);
        response.setRefundEligibility(RefundValidationResponse.RefundEligibility.ELIGIBLE);
        response.setStartedAt(LocalDateTime.now().minusMinutes(10));
        response.setCompletedAt(LocalDateTime.now());
        return response;
    }
}