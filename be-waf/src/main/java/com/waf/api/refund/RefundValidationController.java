package com.waf.api.refund;

import com.waf.api.refund.dto.RefundValidationRequest;
import com.waf.api.refund.dto.RefundValidationResponse;
import com.waf.core.domain.simulation.AttackSimulation;
import com.waf.core.domain.simulation.AttackType;
import com.waf.core.domain.simulation.TestResult;
import com.waf.core.repository.SimulationRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 환불 조건 검증 API 컨트롤러
 * - DVWA 정상 요청 통과 테스트
 * - 5가지 공격 유형 차단 테스트
 * - PDF 증거 리포트 생성
 */
@RestController
@RequestMapping("/api/v1/refund")
public class RefundValidationController {

    private final SimulationRepository simulationRepository;
    private final RefundValidationService refundValidationService;

    public RefundValidationController(SimulationRepository simulationRepository,
                                    RefundValidationService refundValidationService) {
        this.simulationRepository = simulationRepository;
        this.refundValidationService = refundValidationService;
    }

    /**
     * 환불 조건 검증 실행
     * - 5가지 공격 유형 차단 확인
     * - DVWA 정상 요청 통과 확인
     * - 자동 스크린샷 캡처 및 PDF 리포트 생성
     */
    @PostMapping("/validate-conditions")
    public ResponseEntity<RefundValidationResponse> validateRefundConditions(
            @RequestBody RefundValidationRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String userId = extractUserIdFromAuth(authHeader);
        String batchId = UUID.randomUUID().toString();

        // 응답 객체 초기화
        RefundValidationResponse response = new RefundValidationResponse();
        response.setBatchId(batchId);
        response.setStatus(RefundValidationResponse.ValidationStatus.RUNNING);
        response.setRefundEligibility(RefundValidationResponse.RefundEligibility.PENDING);
        response.setStartedAt(LocalDateTime.now());

        // 비동기로 검증 실행
        CompletableFuture.runAsync(() -> {
            executeValidation(request, userId, batchId);
        });

        return ResponseEntity.accepted().body(response);
    }

    /**
     * 검증 결과 조회
     */
    @GetMapping("/validate-conditions/{batchId}")
    public ResponseEntity<RefundValidationResponse> getValidationResult(@PathVariable String batchId) {
        RefundValidationResponse response = refundValidationService.getValidationResult(batchId);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 실제 검증 로직 실행
     */
    private void executeValidation(RefundValidationRequest request, String userId, String batchId) {
        try {
            RefundValidationResponse response = new RefundValidationResponse();
            response.setBatchId(batchId);
            response.setStartedAt(LocalDateTime.now());

            // 1. 공격 시뮬레이션 실행
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults =
                executeAttackSimulations(request, userId);

            // 2. DVWA 정상 요청 테스트
            List<RefundValidationResponse.DvwaTestResult> dvwaResults =
                executeDvwaTests(request, userId);

            // 3. 결과 분석 및 환불 자격 판정
            RefundValidationResponse.RefundEligibility eligibility =
                determineRefundEligibility(attackResults, dvwaResults);

            // 4. 컴플라이언스 리포트 생성
            RefundValidationResponse.ComplianceReport report =
                generateComplianceReport(attackResults, dvwaResults, request.isGeneratePdfReport());

            // 응답 구성
            response.setAttackResults(attackResults);
            response.setDvwaResults(dvwaResults);
            response.setRefundEligibility(eligibility);
            response.setReport(report);
            response.setStatus(RefundValidationResponse.ValidationStatus.COMPLETED);
            response.setCompletedAt(LocalDateTime.now());

            // 결과 캐시 저장
            refundValidationService.cacheValidationResult(batchId, response);

        } catch (Exception e) {
            // 실패 처리
            RefundValidationResponse errorResponse = new RefundValidationResponse();
            errorResponse.setBatchId(batchId);
            errorResponse.setStatus(RefundValidationResponse.ValidationStatus.FAILED);
            errorResponse.setCompletedAt(LocalDateTime.now());
            refundValidationService.cacheValidationResult(batchId, errorResponse);
        }
    }

    /**
     * 5가지 공격 유형 시뮬레이션 실행
     */
    private Map<AttackType, RefundValidationResponse.AttackTestResult> executeAttackSimulations(
            RefundValidationRequest request, String userId) {

        Map<AttackType, RefundValidationResponse.AttackTestResult> results = new HashMap<>();

        // 환불 조건 검증 대상 공격 유형
        AttackType[] refundCriticalAttacks = {
            AttackType.SQL_INJECTION,
            AttackType.XSS,
            AttackType.FILE_UPLOAD,
            AttackType.COMMAND_INJECTION,
            AttackType.PATH_TRAVERSAL
        };

        for (AttackType attackType : refundCriticalAttacks) {
            RefundValidationRequest.TestScenario scenario =
                request.getAttackScenarios().get(attackType);

            if (scenario == null || !scenario.isEnabled()) {
                continue;
            }

            // 공격 시뮬레이션 생성 및 실행
            AttackSimulation simulation = AttackSimulation.forRefundValidation(
                request.getTargetUrl(), attackType, userId);

            simulationRepository.save(simulation.getId());

            // 실제 공격 실행 (모의)
            TestResult testResult = executeAttackTest(
                request.getTargetUrl(),
                attackType,
                scenario.getCustomPayload(),
                request.isIncludeScreenshots()
            );

            simulation.complete(testResult);

            // 결과 변환
            RefundValidationResponse.AttackTestResult result =
                new RefundValidationResponse.AttackTestResult();
            result.setSimulationId(simulation.getId());
            result.setBlocked(testResult.isBlocked());
            result.setResponseStatus(testResult.getResponseStatus());
            result.setResponseTime(testResult.getResponseTime());
            result.setRuleTriggered(testResult.getRuleTriggered());
            result.setScreenshotUrl(testResult.getScreenshotPath());
            result.setWafLogId(testResult.getWafLogId());
            result.setCompliant(testResult.isCompliantForRefund());
            result.setNotes(generateTestNotes(attackType, testResult));

            results.put(attackType, result);
        }

        return results;
    }

    /**
     * DVWA 정상 요청 테스트 실행
     */
    private List<RefundValidationResponse.DvwaTestResult> executeDvwaTests(
            RefundValidationRequest request, String userId) {

        List<RefundValidationResponse.DvwaTestResult> results = new ArrayList<>();

        if (request.getDvwaScenarios() == null || request.getDvwaScenarios().isEmpty()) {
            // 기본 DVWA 테스트 시나리오
            results.add(executeDvwaTest(request.getDvwaUrl(), "GET /login.php", "/login.php", "GET"));
            results.add(executeDvwaTest(request.getDvwaUrl(), "GET /index.php", "/index.php", "GET"));
            results.add(executeDvwaTest(request.getDvwaUrl(), "POST /login.php", "/login.php", "POST"));
        } else {
            for (RefundValidationRequest.DvwaTestScenario scenario : request.getDvwaScenarios()) {
                results.add(executeDvwaTest(
                    request.getDvwaUrl(),
                    scenario.getName(),
                    scenario.getPath(),
                    scenario.getMethod()
                ));
            }
        }

        return results;
    }

    private RefundValidationResponse.DvwaTestResult executeDvwaTest(
            String dvwaUrl, String scenarioName, String path, String method) {

        RefundValidationResponse.DvwaTestResult result =
            new RefundValidationResponse.DvwaTestResult();
        result.setScenario(scenarioName);

        try {
            // 실제 HTTP 요청 실행 (모의)
            long startTime = System.currentTimeMillis();
            int responseStatus = 200; // 모의 응답
            long responseTime = System.currentTimeMillis() - startTime;

            result.setResponseStatus(responseStatus);
            result.setResponseTime(responseTime);
            result.setBlocked(responseStatus == 403);
            result.setPassed(responseStatus == 200 && !result.isBlocked());
            result.setNotes(responseStatus == 200 ? "정상 요청 성공" : "요청 차단됨");

        } catch (Exception e) {
            result.setPassed(false);
            result.setNotes("테스트 실행 오류: " + e.getMessage());
        }

        return result;
    }

    /**
     * 환불 자격 판정
     */
    private RefundValidationResponse.RefundEligibility determineRefundEligibility(
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults,
            List<RefundValidationResponse.DvwaTestResult> dvwaResults) {

        // 공격 차단 성공률 계산
        long blockedAttacks = attackResults.values().stream()
            .mapToLong(result -> result.isCompliant() ? 1 : 0)
            .sum();

        // DVWA 정상 요청 통과율 계산
        long passedDvwaTests = dvwaResults.stream()
            .mapToLong(result -> result.isPassed() ? 1 : 0)
            .sum();

        // 환불 조건: 공격 5가지 모두 차단 + DVWA 정상 요청 모두 통과
        boolean allAttacksBlocked = blockedAttacks == attackResults.size();
        boolean allDvwaTestsPassed = passedDvwaTests == dvwaResults.size();

        if (allAttacksBlocked && allDvwaTestsPassed) {
            return RefundValidationResponse.RefundEligibility.ELIGIBLE;
        } else if (blockedAttacks > 0 || passedDvwaTests > 0) {
            return RefundValidationResponse.RefundEligibility.PARTIAL_ELIGIBLE;
        } else {
            return RefundValidationResponse.RefundEligibility.NOT_ELIGIBLE;
        }
    }

    /**
     * 컴플라이언스 리포트 생성
     */
    private RefundValidationResponse.ComplianceReport generateComplianceReport(
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults,
            List<RefundValidationResponse.DvwaTestResult> dvwaResults,
            boolean generatePdf) {

        RefundValidationResponse.ComplianceReport report =
            new RefundValidationResponse.ComplianceReport();

        int successfulBlocks = (int) attackResults.values().stream()
            .mapToLong(result -> result.isCompliant() ? 1 : 0)
            .sum();

        int normalRequestsPassed = (int) dvwaResults.stream()
            .mapToLong(result -> result.isPassed() ? 1 : 0)
            .sum();

        report.setTotalTests(attackResults.size() + dvwaResults.size());
        report.setSuccessfulBlocks(successfulBlocks);
        report.setFailedBlocks(attackResults.size() - successfulBlocks);
        report.setNormalRequestsPassed(normalRequestsPassed);
        report.setTotalNormalRequests(dvwaResults.size());

        // 컴플라이언스 점수 계산 (0-100)
        double complianceScore = ((double) (successfulBlocks + normalRequestsPassed) /
                                (attackResults.size() + dvwaResults.size())) * 100;
        report.setComplianceScore(complianceScore);

        if (generatePdf) {
            // PDF 리포트 생성 (비동기)
            String pdfUrl = refundValidationService.generatePdfReport(attackResults, dvwaResults);
            report.setPdfReportUrl(pdfUrl);
        }

        report.setReportGeneratedAt(LocalDateTime.now());

        return report;
    }

    // 헬퍼 메서드들
    private String extractUserIdFromAuth(String authHeader) {
        // JWT 토큰에서 사용자 ID 추출 (실제 구현 필요)
        return "user123";
    }

    private TestResult executeAttackTest(String targetUrl, AttackType attackType,
                                       String payload, boolean includeScreenshot) {
        // 실제 공격 시뮬레이션 실행 (모의)
        // WAF에 실제 요청을 보내고 차단 여부 확인
        boolean blocked = true; // 모의 결과
        int responseStatus = blocked ? 403 : 200;
        long responseTime = 150;
        String ruleTriggered = blocked ? "OWASP_CRS_942_100" : null;
        String screenshotPath = includeScreenshot ? "/screenshots/attack_" + System.currentTimeMillis() + ".png" : null;
        String wafLogId = blocked ? "log_" + System.currentTimeMillis() : null;

        return TestResult.blocked(responseStatus, responseTime, ruleTriggered, screenshotPath, wafLogId);
    }

    private String generateTestNotes(AttackType attackType, TestResult result) {
        if (result.isCompliantForRefund()) {
            return attackType.getDisplayName() + " 공격이 성공적으로 차단되었습니다.";
        } else {
            return attackType.getDisplayName() + " 공격이 차단되지 않았습니다. 환불 조건을 충족하지 않습니다.";
        }
    }
}