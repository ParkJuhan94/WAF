package com.waf.infrastructure.external.report;

import com.waf.api.refund.dto.RefundValidationResponse;
import com.waf.core.domain.simulation.AttackType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 환불 조건 검증 PDF 리포트 생성 서비스
 * 공격 차단 증거와 DVWA 테스트 결과를 포함한 공식 문서 생성
 */
@Service
public class PDFReportGenerator {

    private final String reportBasePath = "/app/reports";
    private final String reportBaseUrl = "/api/v1/reports";

    /**
     * 환불 조건 검증 PDF 리포트 생성
     * @param attackResults 공격 테스트 결과
     * @param dvwaResults DVWA 테스트 결과
     * @param batchId 배치 ID
     * @return PDF 리포트 다운로드 URL
     */
    public String generateRefundEvidenceReport(
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults,
            List<RefundValidationResponse.DvwaTestResult> dvwaResults,
            String batchId) {

        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("refund_evidence_%s_%s.pdf", batchId, timestamp);
            String filePath = Paths.get(reportBasePath, fileName).toString();

            // PDF 생성
            generatePdfDocument(attackResults, dvwaResults, filePath, batchId);

            return reportBaseUrl + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }

    /**
     * PDF 문서 생성 (실제로는 iText 라이브러리 사용)
     */
    private void generatePdfDocument(
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults,
            List<RefundValidationResponse.DvwaTestResult> dvwaResults,
            String filePath,
            String batchId) throws IOException {

        // 디렉토리 생성
        File directory = new File(reportBasePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 실제 환경에서는 iText PDF 라이브러리를 사용
        // 여기서는 HTML → PDF 변환 또는 직접 PDF 생성

        // 모의 구현: HTML 템플릿으로 시작
        String htmlContent = generateHtmlReport(attackResults, dvwaResults, batchId);

        // HTML을 임시로 파일에 저장 (실제로는 PDF 변환)
        try (FileWriter writer = new FileWriter(filePath.replace(".pdf", ".html"))) {
            writer.write(htmlContent);
        }

        System.out.println("Generated report: " + filePath);
    }

    /**
     * HTML 리포트 생성 (PDF 변환 전 단계)
     */
    private String generateHtmlReport(
            Map<AttackType, RefundValidationResponse.AttackTestResult> attackResults,
            List<RefundValidationResponse.DvwaTestResult> dvwaResults,
            String batchId) {

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html><html><head>")
            .append("<meta charset='UTF-8'>")
            .append("<title>WAF 환불 조건 검증 리포트</title>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }")
            .append("h1 { color: #2c3e50; border-bottom: 2px solid #3498db; }")
            .append("h2 { color: #34495e; margin-top: 30px; }")
            .append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }")
            .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }")
            .append("th { background-color: #f8f9fa; }")
            .append(".success { color: #27ae60; font-weight: bold; }")
            .append(".failure { color: #e74c3c; font-weight: bold; }")
            .append(".info-box { background-color: #ecf0f1; padding: 15px; margin: 15px 0; border-radius: 5px; }")
            .append("</style>")
            .append("</head><body>");

        // 리포트 헤더
        html.append("<h1>🛡️ WAF 환불 조건 검증 리포트</h1>");
        html.append("<div class='info-box'>");
        html.append("<strong>배치 ID:</strong> ").append(batchId).append("<br>");
        html.append("<strong>생성 시각:</strong> ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ).append("<br>");
        html.append("<strong>리포트 유형:</strong> 환불 조건 검증 증거 문서");
        html.append("</div>");

        // 요약 정보
        html.append("<h2>📋 검증 요약</h2>");
        int totalTests = attackResults.size() + dvwaResults.size();
        long successfulAttackBlocks = attackResults.values().stream()
            .mapToLong(result -> result.isCompliant() ? 1 : 0).sum();
        long passedDvwaTests = dvwaResults.stream()
            .mapToLong(result -> result.isPassed() ? 1 : 0).sum();

        double complianceRate = ((double) (successfulAttackBlocks + passedDvwaTests) / totalTests) * 100;

        html.append("<div class='info-box'>");
        html.append("<strong>총 테스트:</strong> ").append(totalTests).append("개<br>");
        html.append("<strong>성공적으로 차단된 공격:</strong> ").append(successfulAttackBlocks).append("/").append(attackResults.size()).append("<br>");
        html.append("<strong>통과한 정상 요청:</strong> ").append(passedDvwaTests).append("/").append(dvwaResults.size()).append("<br>");
        html.append("<strong>전체 준수율:</strong> ").append(String.format("%.1f", complianceRate)).append("%<br>");
        html.append("<strong>환불 자격:</strong> ");
        if (successfulAttackBlocks == attackResults.size() && passedDvwaTests == dvwaResults.size()) {
            html.append("<span class='success'>✅ 환불 조건 충족</span>");
        } else {
            html.append("<span class='failure'>❌ 환불 조건 미충족</span>");
        }
        html.append("</div>");

        // 공격 차단 테스트 결과
        html.append("<h2>🚨 공격 차단 테스트 결과</h2>");
        html.append("<table>");
        html.append("<tr><th>공격 유형</th><th>차단 여부</th><th>응답 코드</th><th>응답 시간</th><th>트리거된 룰</th><th>준수 여부</th></tr>");

        for (Map.Entry<AttackType, RefundValidationResponse.AttackTestResult> entry : attackResults.entrySet()) {
            AttackType attackType = entry.getKey();
            RefundValidationResponse.AttackTestResult result = entry.getValue();

            html.append("<tr>");
            html.append("<td>").append(attackType.getDisplayName()).append("</td>");
            html.append("<td>").append(result.isBlocked() ? "✅ 차단됨" : "❌ 허용됨").append("</td>");
            html.append("<td>").append(result.getResponseStatus()).append("</td>");
            html.append("<td>").append(result.getResponseTime()).append("ms</td>");
            html.append("<td>").append(result.getRuleTriggered() != null ? result.getRuleTriggered() : "-").append("</td>");
            html.append("<td class='").append(result.isCompliant() ? "success" : "failure").append("'>");
            html.append(result.isCompliant() ? "✅ 준수" : "❌ 미준수").append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        // DVWA 정상 요청 테스트 결과
        html.append("<h2>✅ DVWA 정상 요청 테스트 결과</h2>");
        html.append("<table>");
        html.append("<tr><th>테스트 시나리오</th><th>통과 여부</th><th>응답 코드</th><th>응답 시간</th><th>비고</th></tr>");

        for (RefundValidationResponse.DvwaTestResult result : dvwaResults) {
            html.append("<tr>");
            html.append("<td>").append(result.getScenario()).append("</td>");
            html.append("<td class='").append(result.isPassed() ? "success" : "failure").append("'>");
            html.append(result.isPassed() ? "✅ 통과" : "❌ 실패").append("</td>");
            html.append("<td>").append(result.getResponseStatus()).append("</td>");
            html.append("<td>").append(result.getResponseTime()).append("ms</td>");
            html.append("<td>").append(result.getNotes() != null ? result.getNotes() : "-").append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        // 환불 조건 체크리스트
        html.append("<h2>✔️ 환불 조건 체크리스트</h2>");
        html.append("<div class='info-box'>");
        html.append("<h3>필수 조건:</h3>");
        html.append("<ul>");

        for (AttackType attackType : AttackType.values()) {
            if (!attackType.isRefundCritical()) continue;

            RefundValidationResponse.AttackTestResult result = attackResults.get(attackType);
            boolean passed = result != null && result.isCompliant();

            html.append("<li>");
            html.append(passed ? "✅" : "❌").append(" ");
            html.append(attackType.getDisplayName()).append(" 공격 차단: ");
            html.append("<strong>").append(passed ? "성공" : "실패").append("</strong>");
            html.append("</li>");
        }

        html.append("<li>");
        boolean allDvwaPassed = dvwaResults.stream().allMatch(RefundValidationResponse.DvwaTestResult::isPassed);
        html.append(allDvwaPassed ? "✅" : "❌").append(" ");
        html.append("DVWA 정상 요청 통과: ");
        html.append("<strong>").append(allDvwaPassed ? "성공" : "실패").append("</strong>");
        html.append("</li>");

        html.append("</ul>");
        html.append("</div>");

        // 결론
        html.append("<h2>📄 결론</h2>");
        html.append("<div class='info-box'>");
        boolean refundEligible = (successfulAttackBlocks == attackResults.size()) &&
                                (passedDvwaTests == dvwaResults.size());

        if (refundEligible) {
            html.append("<h3 class='success'>✅ 환불 조건 충족</h3>");
            html.append("<p>모든 공격이 성공적으로 차단되었고, 정상 요청은 모두 통과하였습니다. ");
            html.append("본 WAF 시스템은 환불 조건을 완전히 충족합니다.</p>");
        } else {
            html.append("<h3 class='failure'>❌ 환불 조건 미충족</h3>");
            html.append("<p>일부 테스트에서 요구사항을 충족하지 못했습니다. ");
            html.append("환불 조건을 충족하기 위해서는 추가 설정이 필요합니다.</p>");
        }
        html.append("</div>");

        html.append("<footer>");
        html.append("<p><small>본 리포트는 WAF Console 시스템에 의해 자동 생성되었습니다. ");
        html.append("생성 시각: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        html.append("</small></p>");
        html.append("</footer>");

        html.append("</body></html>");

        return html.toString();
    }

    /**
     * 리포트 파일 정리
     * @param olderThanDays 지정된 일수보다 오래된 파일 삭제
     */
    public void cleanupOldReports(int olderThanDays) {
        try {
            File reportDir = new File(reportBasePath);
            if (!reportDir.exists()) return;

            long cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L);

            File[] files = reportDir.listFiles((dir, name) ->
                name.endsWith(".pdf") || name.endsWith(".html"));

            if (files != null) {
                for (File file : files) {
                    if (file.lastModified() < cutoffTime) {
                        file.delete();
                        System.out.println("Deleted old report: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to cleanup reports: " + e.getMessage());
        }
    }

    /**
     * 리포트 통계 정보 조회
     */
    public ReportStats getReportStats() {
        File reportDir = new File(reportBasePath);
        if (!reportDir.exists()) {
            return new ReportStats(0, 0, 0);
        }

        File[] files = reportDir.listFiles();
        if (files == null) {
            return new ReportStats(0, 0, 0);
        }

        long totalFiles = files.length;
        long totalSize = 0;
        long lastModified = 0;

        for (File file : files) {
            totalSize += file.length();
            if (file.lastModified() > lastModified) {
                lastModified = file.lastModified();
            }
        }

        return new ReportStats(totalFiles, totalSize, lastModified);
    }

    public static class ReportStats {
        private final long totalFiles;
        private final long totalSize;
        private final long lastModified;

        public ReportStats(long totalFiles, long totalSize, long lastModified) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.lastModified = lastModified;
        }

        public long getTotalFiles() { return totalFiles; }
        public long getTotalSize() { return totalSize; }
        public long getLastModified() { return lastModified; }
    }
}