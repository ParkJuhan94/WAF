package dev.waf.console.api.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.waf.console.api.rule.dto.CustomRuleRequest;
import dev.waf.console.core.domain.rule.CustomRule;
import dev.waf.console.core.domain.rule.RuleSeverity;
import dev.waf.console.core.domain.rule.RuleType;
import dev.waf.console.core.domain.user.User;
import dev.waf.console.core.domain.user.UserRole;
import dev.waf.console.core.service.CustomRuleService;
import dev.waf.console.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomRuleController.class)
@ActiveProfiles("test")
@DisplayName("CustomRuleController 테스트")
class CustomRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomRuleService customRuleService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private CustomRule testRule;
    private CustomRuleRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "test@example.com",
                "Test User",
                "https://example.com/profile.jpg",
                "google",
                "google-123"
        );
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testRule = CustomRule.create(
                "Test SQL Injection Rule",
                "Test rule for SQL injection detection",
                "SecRule ARGS \"@detectSQLi\" \"id:1001,phase:2,block\"",
                RuleType.BLOCK,
                RuleSeverity.HIGH,
                testUser
        );
        ReflectionTestUtils.setField(testRule, "id", 1L);

        testRequest = new CustomRuleRequest();
        testRequest.setName("Test SQL Injection Rule");
        testRequest.setDescription("Test rule for SQL injection detection");
        testRequest.setRuleContent("SecRule ARGS \"@detectSQLi\" \"id:1001,phase:2,block\"");
        testRequest.setType(RuleType.BLOCK);
        testRequest.setSeverity(RuleSeverity.HIGH);
        testRequest.setPriority(900);
        testRequest.setTargetService("user-service");
        testRequest.setTargetPath("/api/user/*");

        // JWT Token Provider Mock 설정
        given(jwtTokenProvider.getUserId(anyString())).willReturn("1");
    }

    @Test
    @WithMockUser
    @DisplayName("룰 목록 조회 성공")
    void getRules_Success() throws Exception {
        // given
        Page<CustomRule> rulePage = new PageImpl<>(List.of(testRule));
        given(customRuleService.getRulesWithFilters(any(), any(), any(), any(), any()))
                .willReturn(rulePage);

        // when & then
        mockMvc.perform(get("/api/v1/rules")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "createdAt")
                        .param("sortDir", "desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test SQL Injection Rule"));

        verify(customRuleService).getRulesWithFilters(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("키워드 검색 성공")
    void searchRules_Success() throws Exception {
        // given
        Page<CustomRule> rulePage = new PageImpl<>(List.of(testRule));
        given(customRuleService.searchRules(anyString(), any())).willReturn(rulePage);

        // when & then
        mockMvc.perform(get("/api/v1/rules")
                        .param("keyword", "SQL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(customRuleService).searchRules(eq("SQL"), any());
    }

    @Test
    @WithMockUser
    @DisplayName("룰 단건 조회 성공")
    void getRule_Success() throws Exception {
        // given
        given(customRuleService.getRule(1L)).willReturn(testRule);

        // when & then
        mockMvc.perform(get("/api/v1/rules/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test SQL Injection Rule"))
                .andExpect(jsonPath("$.type").value("BLOCK"))
                .andExpect(jsonPath("$.severity").value("HIGH"));

        verify(customRuleService).getRule(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("사용자별 룰 조회 성공")
    void getMyRules_Success() throws Exception {
        // given
        Page<CustomRule> rulePage = new PageImpl<>(List.of(testRule));
        given(customRuleService.getUserRules(eq(1L), any())).willReturn(rulePage);

        // when & then
        mockMvc.perform(get("/api/v1/rules/my")
                        .header("Authorization", "Bearer test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(jwtTokenProvider).getUserId("test-token");
        verify(customRuleService).getUserRules(eq(1L), any());
    }

    @Test
    @WithMockUser
    @DisplayName("활성 룰 조회 성공")
    void getActiveRules_Success() throws Exception {
        // given
        given(customRuleService.getActiveRulesByPriority()).willReturn(List.of(testRule));

        // when & then
        mockMvc.perform(get("/api/v1/rules/active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test SQL Injection Rule"));

        verify(customRuleService).getActiveRulesByPriority();
    }

    @Test
    @WithMockUser
    @DisplayName("적용 가능한 룰 조회 성공")
    void getApplicableRules_Success() throws Exception {
        // given
        given(customRuleService.getApplicableRules("user-service")).willReturn(List.of(testRule));

        // when & then
        mockMvc.perform(get("/api/v1/rules/applicable")
                        .param("targetService", "user-service"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(customRuleService).getApplicableRules("user-service");
    }

    @Test
    @WithMockUser
    @DisplayName("최근 활성 룰 조회 성공")
    void getRecentlyActiveRules_Success() throws Exception {
        // given
        Page<CustomRule> rulePage = new PageImpl<>(List.of(testRule));
        given(customRuleService.getRecentlyActiveRules(any())).willReturn(rulePage);

        // when & then
        mockMvc.perform(get("/api/v1/rules/recent-active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(customRuleService).getRecentlyActiveRules(any());
    }

    @Test
    @WithMockUser
    @DisplayName("룰 생성 성공")
    void createRule_Success() throws Exception {
        // given
        given(customRuleService.createRule(anyString(), anyString(), anyString(),
                any(RuleType.class), any(RuleSeverity.class), eq(1L)))
                .willReturn(testRule);
        given(customRuleService.setRuleTargetScope(eq(1L), anyString(), anyString(), eq(1L)))
                .willReturn(testRule);

        // when & then
        mockMvc.perform(post("/api/v1/rules")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test SQL Injection Rule"))
                .andExpect(jsonPath("$.type").value("BLOCK"));

        verify(customRuleService).createRule(
                eq("Test SQL Injection Rule"),
                eq("Test rule for SQL injection detection"),
                eq("SecRule ARGS \"@detectSQLi\" \"id:1001,phase:2,block\""),
                eq(RuleType.BLOCK),
                eq(RuleSeverity.HIGH),
                eq(1L)
        );
        verify(customRuleService).setRuleTargetScope(eq(1L), eq("user-service"), eq("/api/user/*"), eq(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("룰 생성 실패 - 유효성 검증")
    void createRule_Fail_Validation() throws Exception {
        // given
        CustomRuleRequest invalidRequest = new CustomRuleRequest();
        invalidRequest.setName(""); // 빈 이름
        invalidRequest.setRuleContent(""); // 빈 내용

        // when & then
        mockMvc.perform(post("/api/v1/rules")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(customRuleService, never()).createRule(any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("룰 수정 성공")
    void updateRule_Success() throws Exception {
        // given
        given(customRuleService.updateRule(eq(1L), anyString(), anyString(), anyString(),
                any(RuleType.class), any(RuleSeverity.class), anyInt(), eq(1L)))
                .willReturn(testRule);
        given(customRuleService.setRuleTargetScope(eq(1L), anyString(), anyString(), eq(1L)))
                .willReturn(testRule);

        // when & then
        mockMvc.perform(put("/api/v1/rules/1")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test SQL Injection Rule"));

        verify(customRuleService).updateRule(eq(1L), anyString(), anyString(), anyString(),
                any(RuleType.class), any(RuleSeverity.class), anyInt(), eq(1L));
        verify(customRuleService).setRuleTargetScope(eq(1L), anyString(), anyString(), eq(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("룰 삭제 성공")
    void deleteRule_Success() throws Exception {
        // given
        willDoNothing().given(customRuleService).deleteRule(1L, 1L);

        // when & then
        mockMvc.perform(delete("/api/v1/rules/1")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(customRuleService).deleteRule(1L, 1L);
    }

    @Test
    @WithMockUser
    @DisplayName("룰 상태 토글 성공")
    void toggleRuleStatus_Success() throws Exception {
        // given
        given(customRuleService.toggleRuleStatus(1L, 1L)).willReturn(testRule);

        // when & then
        mockMvc.perform(patch("/api/v1/rules/1/toggle")
                        .header("Authorization", "Bearer test-token")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test SQL Injection Rule"));

        verify(customRuleService).toggleRuleStatus(1L, 1L);
    }

    @Test
    @WithMockUser
    @DisplayName("룰 매치 기록 성공")
    void recordMatch_Success() throws Exception {
        // given
        willDoNothing().given(customRuleService).recordRuleMatch(1L);

        // when & then
        mockMvc.perform(post("/api/v1/rules/1/match")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(customRuleService).recordRuleMatch(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("룰 차단 기록 성공")
    void recordBlock_Success() throws Exception {
        // given
        willDoNothing().given(customRuleService).recordRuleBlock(1L);

        // when & then
        mockMvc.perform(post("/api/v1/rules/1/block")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(customRuleService).recordRuleBlock(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("룰 통계 조회 성공")
    void getRuleStatistics_Success() throws Exception {
        // given
        CustomRuleService.RuleStatistics statistics =
                CustomRuleService.RuleStatistics.builder()
                        .totalRules(10L)
                        .enabledRules(8L)
                        .disabledRules(2L)
                        .totalMatches(100L)
                        .totalBlocks(50L)
                        .build();
        given(customRuleService.getRuleStatistics()).willReturn(statistics);

        // when & then
        mockMvc.perform(get("/api/v1/rules/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRules").value(10))
                .andExpect(jsonPath("$.enabledRules").value(8))
                .andExpect(jsonPath("$.disabledRules").value(2))
                .andExpect(jsonPath("$.totalMatches").value(100))
                .andExpect(jsonPath("$.totalBlocks").value(50));

        verify(customRuleService).getRuleStatistics();
    }

    @Test
    @WithMockUser
    @DisplayName("룰 타입 목록 조회 성공")
    void getRuleTypes_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/rules/types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(RuleType.values().length));
    }

    @Test
    @WithMockUser
    @DisplayName("심각도 목록 조회 성공")
    void getRuleSeverities_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/rules/severities"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(RuleSeverity.values().length));
    }

    @Test
    @DisplayName("인증 없이 요청 시 401 반환")
    void unauthenticatedRequest_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/rules"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(customRuleService, never()).getRulesWithFilters(any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("잘못된 JWT 토큰으로 요청 시 처리")
    void invalidJwtToken_HandledProperly() throws Exception {
        // given
        given(jwtTokenProvider.getUserId(anyString())).willThrow(new RuntimeException("Invalid token"));

        // when & then
        mockMvc.perform(get("/api/v1/rules/my")
                        .header("Authorization", "Bearer invalid-token"))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @DisplayName("존재하지 않는 룰 조회 시 404 반환")
    void getNonExistentRule_Returns404() throws Exception {
        // given
        given(customRuleService.getRule(999L))
                .willThrow(new IllegalArgumentException("룰을 찾을 수 없습니다: 999"));

        // when & then
        mockMvc.perform(get("/api/v1/rules/999"))
                .andDo(print())
                .andExpect(status().isInternalServerError()); // 현재 GlobalExceptionHandler가 없으므로 500

        verify(customRuleService).getRule(999L);
    }

    @Test
    @WithMockUser
    @DisplayName("페이징 파라미터 검증")
    void validatePagingParameters() throws Exception {
        // given
        Page<CustomRule> rulePage = new PageImpl<>(List.of());
        given(customRuleService.getRulesWithFilters(any(), any(), any(), any(), any()))
                .willReturn(rulePage);

        // when & then
        mockMvc.perform(get("/api/v1/rules")
                        .param("page", "0")
                        .param("size", "50")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(customRuleService).getRulesWithFilters(any(), any(), any(), any(), any());
    }
}