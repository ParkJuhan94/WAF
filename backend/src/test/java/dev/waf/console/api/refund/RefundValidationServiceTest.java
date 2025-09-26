package dev.waf.console.api.refund;

import dev.waf.console.api.refund.dto.RefundValidationResponse;
import dev.waf.console.core.domain.simulation.AttackType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("환불 검증 서비스 테스트")
class RefundValidationServiceTest {

    private RefundValidationService refundValidationService;

    @BeforeEach
    void setUp() {
        refundValidationService = new RefundValidationService();
    }

    @Nested
    @DisplayName("검증 결과 캐시")
    class ValidationResultCache {

        @Test
        @DisplayName("검증 결과를 캐시에 저장하고 조회할 수 있다")
        void cacheAndRetrieveValidationResult() {
            // given
            String batchId = "test-batch-123";
            RefundValidationResponse response = createSampleResponse(batchId);

            // when
            refundValidationService.cacheValidationResult(batchId, response);
            RefundValidationResponse retrieved = refundValidationService.getValidationResult(batchId);

            // then
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getBatchId()).isEqualTo(batchId);
            assertThat(retrieved.getStatus()).isEqualTo(RefundValidationResponse.ValidationStatus.COMPLETED);
        }

        @Test
        @DisplayName("존재하지 않는 배치 ID로 조회하면 null을 반환한다")
        void returnNullForNonExistentBatchId() {
            // given
            String nonExistentBatchId = "non-existent-batch";

            // when
            RefundValidationResponse result = refundValidationService.getValidationResult(nonExistentBatchId);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("같은 배치 ID로 여러 번 저장하면 마지막 값이 유지된다")
        void overwriteExistingCacheEntry() {
            // given
            String batchId = "test-batch-456";
            RefundValidationResponse firstResponse = createSampleResponse(batchId);
            firstResponse.setStatus(RefundValidationResponse.ValidationStatus.RUNNING);

            RefundValidationResponse secondResponse = createSampleResponse(batchId);
            secondResponse.setStatus(RefundValidationResponse.ValidationStatus.COMPLETED);

            // when
            refundValidationService.cacheValidationResult(batchId, firstResponse);
            refundValidationService.cacheValidationResult(batchId, secondResponse);

            RefundValidationResponse retrieved = refundValidationService.getValidationResult(batchId);

            // then
            assertThat(retrieved.getStatus()).isEqualTo(RefundValidationResponse.ValidationStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("PDF 리포트 생성")
    class PdfReportGeneration {

        @Test
        @DisplayName("PDF 리포트 URL을 생성할 수 있다")
        void generatePdfReportUrl() {
            // given
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults = Collections.emptyMap();
            List<RefundValidationResponse.DvwaTestResult> dvwaResults = Collections.emptyList();

            // when
            String pdfUrl = refundValidationService.generatePdfReport(attackResults, dvwaResults);

            // then
            assertThat(pdfUrl).isNotNull();
            assertThat(pdfUrl).startsWith("/reports/refund_evidence_");
            assertThat(pdfUrl).endsWith(".pdf");
        }

        @Test
        @DisplayName("각 호출마다 다른 PDF URL을 생성한다")
        void generateUniquePdfUrls() throws InterruptedException {
            // given
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults = Collections.emptyMap();
            List<RefundValidationResponse.DvwaTestResult> dvwaResults = Collections.emptyList();

            // when
            String firstUrl = refundValidationService.generatePdfReport(attackResults, dvwaResults);
            Thread.sleep(1); // 타임스탬프 차이를 보장하기 위해 1ms 대기
            String secondUrl = refundValidationService.generatePdfReport(attackResults, dvwaResults);

            // then
            assertThat(firstUrl).isNotEqualTo(secondUrl);
        }
    }

    @Nested
    @DisplayName("캐시 정리")
    class CacheClear {

        @Test
        @DisplayName("만료된 결과를 정리할 수 있다")
        void clearExpiredResults() {
            // given
            String batchId = "test-batch-789";
            RefundValidationResponse response = createSampleResponse(batchId);
            refundValidationService.cacheValidationResult(batchId, response);

            // when
            refundValidationService.clearExpiredResults();

            // then
            RefundValidationResponse retrieved = refundValidationService.getValidationResult(batchId);
            assertThat(retrieved).isNull();
        }

        @Test
        @DisplayName("캐시를 정리한 후 새로운 결과를 저장할 수 있다")
        void cacheNewResultAfterClear() {
            // given
            String batchId = "test-batch-clear";
            RefundValidationResponse response = createSampleResponse(batchId);

            // when
            refundValidationService.clearExpiredResults();
            refundValidationService.cacheValidationResult(batchId, response);

            // then
            RefundValidationResponse retrieved = refundValidationService.getValidationResult(batchId);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getBatchId()).isEqualTo(batchId);
        }
    }

    private RefundValidationResponse createSampleResponse(String batchId) {
        RefundValidationResponse response = new RefundValidationResponse();
        response.setBatchId(batchId);
        response.setStatus(RefundValidationResponse.ValidationStatus.COMPLETED);
        response.setRefundEligibility(RefundValidationResponse.RefundEligibility.ELIGIBLE);
        response.setStartedAt(LocalDateTime.now().minusMinutes(10));
        response.setCompletedAt(LocalDateTime.now());
        return response;
    }
}