package dev.waf.console.api.rule;

import dev.waf.console.api.rule.dto.CustomRuleRequest;
import dev.waf.console.api.rule.dto.CustomRuleResponse;
import dev.waf.console.core.domain.rule.CustomRule;
import dev.waf.console.core.domain.rule.RuleSeverity;
import dev.waf.console.core.domain.rule.RuleType;
import dev.waf.console.core.service.CustomRuleService;
import dev.waf.console.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rules")
@RequiredArgsConstructor
@Slf4j
public class CustomRuleController {

    private final CustomRuleService customRuleService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 모든 룰 조회 (페이징, 필터링, 검색)
     */
    @GetMapping
    public ResponseEntity<Page<CustomRuleResponse>> getRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) RuleType type,
            @RequestParam(required = false) RuleSeverity severity,
            @RequestParam(required = false) String targetService,
            @RequestParam(required = false) String keyword) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CustomRule> rules;
        if (keyword != null && !keyword.trim().isEmpty()) {
            rules = customRuleService.searchRules(keyword.trim(), pageable);
        } else {
            rules = customRuleService.getRulesWithFilters(enabled, type, severity, targetService, pageable);
        }

        Page<CustomRuleResponse> response = rules.map(CustomRuleResponse::from);
        return ResponseEntity.ok(response);
    }

    /**
     * 룰 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomRuleResponse> getRule(@PathVariable Long id) {
        CustomRule rule = customRuleService.getRule(id);
        return ResponseEntity.ok(CustomRuleResponse.from(rule));
    }

    /**
     * 사용자별 룰 조회
     */
    @GetMapping("/my")
    public ResponseEntity<Page<CustomRuleResponse>> getMyRules(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = getUserIdFromToken(token);
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CustomRule> rules = customRuleService.getUserRules(userId, pageable);
        Page<CustomRuleResponse> response = rules.map(CustomRuleResponse::from);
        return ResponseEntity.ok(response);
    }

    /**
     * 활성화된 룰 조회 (우선순위 순)
     */
    @GetMapping("/active")
    public ResponseEntity<List<CustomRuleResponse>> getActiveRules() {
        List<CustomRule> rules = customRuleService.getActiveRulesByPriority();
        List<CustomRuleResponse> response = rules.stream()
                .map(CustomRuleResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 서비스에 적용 가능한 룰 조회
     */
    @GetMapping("/applicable")
    public ResponseEntity<List<CustomRuleResponse>> getApplicableRules(
            @RequestParam String targetService) {
        List<CustomRule> rules = customRuleService.getApplicableRules(targetService);
        List<CustomRuleResponse> response = rules.stream()
                .map(CustomRuleResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 활성 룰 조회
     */
    @GetMapping("/recent-active")
    public ResponseEntity<Page<CustomRuleResponse>> getRecentlyActiveRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomRule> rules = customRuleService.getRecentlyActiveRules(pageable);
        Page<CustomRuleResponse> response = rules.map(CustomRuleResponse::from);
        return ResponseEntity.ok(response);
    }

    /**
     * 룰 생성
     */
    @PostMapping
    public ResponseEntity<CustomRuleResponse> createRule(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CustomRuleRequest request) {

        Long userId = getUserIdFromToken(token);
        CustomRule rule = customRuleService.createRule(
                request.getName(),
                request.getDescription(),
                request.getRuleContent(),
                request.getType(),
                request.getSeverity(),
                userId
        );

        // 대상 범위 설정 (있는 경우)
        if (request.getTargetService() != null || request.getTargetPath() != null) {
            rule = customRuleService.setRuleTargetScope(
                    rule.getId(),
                    request.getTargetService(),
                    request.getTargetPath(),
                    userId
            );
        }

        return ResponseEntity.ok(CustomRuleResponse.from(rule));
    }

    /**
     * 룰 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomRuleResponse> updateRule(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CustomRuleRequest request) {

        Long userId = getUserIdFromToken(token);
        CustomRule rule = customRuleService.updateRule(
                id,
                request.getName(),
                request.getDescription(),
                request.getRuleContent(),
                request.getType(),
                request.getSeverity(),
                request.getPriority(),
                userId
        );

        // 대상 범위 설정
        rule = customRuleService.setRuleTargetScope(
                id,
                request.getTargetService(),
                request.getTargetPath(),
                userId
        );

        return ResponseEntity.ok(CustomRuleResponse.from(rule));
    }

    /**
     * 룰 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long userId = getUserIdFromToken(token);
        customRuleService.deleteRule(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 룰 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CustomRuleResponse> toggleRuleStatus(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long userId = getUserIdFromToken(token);
        CustomRule rule = customRuleService.toggleRuleStatus(id, userId);
        return ResponseEntity.ok(CustomRuleResponse.from(rule));
    }

    /**
     * 룰 매치 기록
     */
    @PostMapping("/{id}/match")
    public ResponseEntity<Void> recordMatch(@PathVariable Long id) {
        customRuleService.recordRuleMatch(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 룰 차단 기록
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> recordBlock(@PathVariable Long id) {
        customRuleService.recordRuleBlock(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 룰 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<CustomRuleService.RuleStatistics> getRuleStatistics() {
        CustomRuleService.RuleStatistics statistics = customRuleService.getRuleStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 룰 타입 목록 조회
     */
    @GetMapping("/types")
    public ResponseEntity<RuleType[]> getRuleTypes() {
        return ResponseEntity.ok(RuleType.values());
    }

    /**
     * 심각도 목록 조회
     */
    @GetMapping("/severities")
    public ResponseEntity<RuleSeverity[]> getRuleSeverities() {
        return ResponseEntity.ok(RuleSeverity.values());
    }

    private Long getUserIdFromToken(String token) {
        String bearerToken = token.substring(7); // Remove "Bearer "
        String userIdStr = jwtTokenProvider.getUserId(bearerToken);
        return Long.parseLong(userIdStr);
    }
}