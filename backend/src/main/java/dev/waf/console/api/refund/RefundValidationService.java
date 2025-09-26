package dev.waf.console.api.refund;

import dev.waf.console.api.refund.dto.RefundValidationResponse;
import dev.waf.console.core.domain.simulation.AttackType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 환불 조건 검증 서비스
 */
@Service
public class RefundValidationService {

    // 간단한 인메모리 캐시 (실제 환경에서는 Redis 사용)
    private final Map<String, RefundValidationResponse> validationCache = new ConcurrentHashMap<>();

    /**
     * 검증 결과 조회
     */
    public RefundValidationResponse getValidationResult(String batchId) {
        return validationCache.get(batchId);
    }

    /**
     * 검증 결과 캐시 저장
     */
    public void cacheValidationResult(String batchId, RefundValidationResponse response) {
        validationCache.put(batchId, response);
    }

    /**
     * PDF 리포트 생성
     */
    public String generatePdfReport(Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults,
                                  List<RefundValidationResponse.DvwaTestResult> dvwaResults) {
        // PDF 생성 로직은 별도 서비스에서 구현
        // 여기서는 모의 URL 반환
        return "/reports/refund_evidence_" + System.currentTimeMillis() + ".pdf";
    }

    /**
     * 캐시 정리 (TTL 기반 또는 수동)
     */
    public void clearExpiredResults() {
        // 실제 환경에서는 TTL이나 스케줄러를 통해 정리
        validationCache.clear();
    }
}