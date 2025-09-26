package com.waf.infrastructure.external.report;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * 환불 조건 검증을 위한 스크린샷 캡처 서비스
 * WAF 차단 화면을 자동으로 캡처하여 증거로 활용
 */
@Service
public class ScreenshotCaptureService {

    private final String screenshotBasePath = "/app/screenshots";
    private final String screenshotBaseUrl = "/api/v1/screenshots";

    /**
     * WAF 차단 화면 스크린샷 캡처
     * @param targetUrl 대상 URL
     * @param attackType 공격 유형
     * @param payload 공격 페이로드
     * @return 스크린샷 파일 경로
     */
    public CompletableFuture<String> captureBlockedAttack(String targetUrl, String attackType, String payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("blocked_%s_%s.png",
                    sanitizeFileName(attackType), timestamp);
                String filePath = Paths.get(screenshotBasePath, fileName).toString();

                // 실제 스크린샷 캡처 로직
                boolean captured = captureScreenshotWithSelenium(targetUrl, payload, filePath);

                if (captured) {
                    return screenshotBaseUrl + "/" + fileName;
                } else {
                    throw new RuntimeException("Failed to capture screenshot");
                }

            } catch (Exception e) {
                throw new RuntimeException("Screenshot capture failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * DVWA 정상 요청 화면 스크린샷 캡처
     * @param dvwaUrl DVWA URL
     * @param path 요청 경로
     * @return 스크린샷 파일 경로
     */
    public CompletableFuture<String> captureDvwaNormalRequest(String dvwaUrl, String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String fileName = String.format("dvwa_normal_%s_%s.png",
                    sanitizeFileName(path), timestamp);
                String filePath = Paths.get(screenshotBasePath, fileName).toString();

                boolean captured = captureScreenshotWithSelenium(dvwaUrl + path, null, filePath);

                if (captured) {
                    return screenshotBaseUrl + "/" + fileName;
                } else {
                    throw new RuntimeException("Failed to capture DVWA screenshot");
                }

            } catch (Exception e) {
                throw new RuntimeException("DVWA screenshot capture failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 배치 스크린샷 캡처 (여러 공격 동시 처리)
     * @param captureRequests 캡처 요청 목록
     * @return 캡처된 스크린샷 URL 목록
     */
    public CompletableFuture<java.util.Map<String, String>> captureBatchScreenshots(
            java.util.List<ScreenshotRequest> captureRequests) {

        return CompletableFuture.supplyAsync(() -> {
            java.util.Map<String, String> results = new java.util.concurrent.ConcurrentHashMap<>();

            captureRequests.parallelStream().forEach(request -> {
                try {
                    String screenshotUrl;
                    if ("ATTACK".equals(request.getType())) {
                        screenshotUrl = captureBlockedAttack(
                            request.getUrl(),
                            request.getAttackType(),
                            request.getPayload()
                        ).get();
                    } else {
                        screenshotUrl = captureDvwaNormalRequest(
                            request.getUrl(),
                            request.getPath()
                        ).get();
                    }
                    results.put(request.getId(), screenshotUrl);
                } catch (Exception e) {
                    results.put(request.getId(), "ERROR: " + e.getMessage());
                }
            });

            return results;
        });
    }

    /**
     * Selenium을 사용한 실제 스크린샷 캡처
     * 실제 환경에서는 WebDriver 설정 및 구현 필요
     */
    private boolean captureScreenshotWithSelenium(String url, String payload, String filePath) {
        try {
            // 실제 Selenium WebDriver 구현 부분
            // 헤드리스 Chrome을 사용하여 스크린샷 캡처

            // 모의 구현 (실제로는 WebDriver 사용)
            System.out.println("Capturing screenshot: " + url);
            System.out.println("Payload: " + payload);
            System.out.println("Output: " + filePath);

            // 디렉토리 생성
            File directory = new File(screenshotBasePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 모의 파일 생성 (실제로는 스크린샷 저장)
            File screenshotFile = new File(filePath);
            screenshotFile.createNewFile();

            return true;

        } catch (Exception e) {
            System.err.println("Screenshot capture error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 파일명에 사용할 수 없는 문자 제거
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unknown";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
    }

    /**
     * 스크린샷 요청 정보
     */
    public static class ScreenshotRequest {
        private String id;
        private String type; // "ATTACK" or "NORMAL"
        private String url;
        private String attackType;
        private String payload;
        private String path;

        public ScreenshotRequest(String id, String type, String url) {
            this.id = id;
            this.type = type;
            this.url = url;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getAttackType() { return attackType; }
        public void setAttackType(String attackType) { this.attackType = attackType; }
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }

    /**
     * 저장된 스크린샷 파일 정리
     * @param olderThanDays 지정된 일수보다 오래된 파일 삭제
     */
    public void cleanupOldScreenshots(int olderThanDays) {
        try {
            File screenshotDir = new File(screenshotBasePath);
            if (!screenshotDir.exists()) return;

            long cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L);

            File[] files = screenshotDir.listFiles((dir, name) ->
                name.endsWith(".png") || name.endsWith(".jpg"));

            if (files != null) {
                for (File file : files) {
                    if (file.lastModified() < cutoffTime) {
                        file.delete();
                        System.out.println("Deleted old screenshot: " + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to cleanup screenshots: " + e.getMessage());
        }
    }
}