package dev.waf.console.dashboard.api;

import dev.waf.console.dashboard.api.dto.AttackEventResponse;
import dev.waf.console.dashboard.api.dto.TrafficDataResponse;
import dev.waf.console.dashboard.api.dto.WAFStatsResponse;
import dev.waf.console.dashboard.api.dto.WAFStatusResponse;
import dev.waf.console.dashboard.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DashboardController 통합 테스트
 */
@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController API 테스트")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("GET /api/dashboard/stats - 통계 조회 성공")
    void getDashboardStats_shouldReturn200WithData() throws Exception {
        // Given
        WAFStatsResponse mockStats = new WAFStatsResponse(
            1000L,
            100L,
            900L,
            10.0,
            45.5,
            3600L
        );
        when(dashboardService.getWAFStats()).thenReturn(mockStats);

        // When & Then
        mockMvc.perform(get("/api/dashboard/stats"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalRequests").value(1000))
            .andExpect(jsonPath("$.data.blockedRequests").value(100))
            .andExpect(jsonPath("$.data.allowedRequests").value(900))
            .andExpect(jsonPath("$.data.blockRate").value(10.0))
            .andExpect(jsonPath("$.data.avgResponseTime").value(45.5))
            .andExpect(jsonPath("$.data.uptime").value(3600))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/status - 상태 조회 성공")
    void getWAFStatus_shouldReturn200WithData() throws Exception {
        // Given
        WAFStatusResponse mockStatus = new WAFStatusResponse(
            "active",
            "1.0.0",
            "2024-12-10T10:00:00",
            "2024-12-10T14:00:00",
            50,
            40
        );
        when(dashboardService.getWAFStatus()).thenReturn(mockStatus);

        // When & Then
        mockMvc.perform(get("/api/dashboard/status"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("active"))
            .andExpect(jsonPath("$.data.version").value("1.0.0"))
            .andExpect(jsonPath("$.data.rulesCount").value(50))
            .andExpect(jsonPath("$.data.activeRulesCount").value(40))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/traffic - 트래픽 데이터 조회 성공")
    void getTrafficData_shouldReturn200WithData() throws Exception {
        // Given
        List<TrafficDataResponse> mockTraffic = List.of(
            new TrafficDataResponse("2024-12-10T14:00:00", 234L, 12L, 222L, 42.5),
            new TrafficDataResponse("2024-12-10T15:00:00", 198L, 8L, 190L, 38.2)
        );
        when(dashboardService.getTrafficData(anyInt())).thenReturn(mockTraffic);

        // When & Then
        mockMvc.perform(get("/api/dashboard/traffic").param("hours", "24"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].timestamp").value("2024-12-10T14:00:00"))
            .andExpect(jsonPath("$.data[0].totalRequests").value(234))
            .andExpect(jsonPath("$.data[0].blockedRequests").value(12))
            .andExpect(jsonPath("$.data[0].allowedRequests").value(222))
            .andExpect(jsonPath("$.data[0].responseTime").value(42.5))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/traffic - 잘못된 파라미터 (0)")
    void getTrafficData_withInvalidParameter_shouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/traffic").param("hours", "0"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/traffic - 잘못된 파라미터 (너무 큰 값)")
    void getTrafficData_withTooLargeParameter_shouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/traffic").param("hours", "200"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/attacks - 공격 이벤트 조회 성공")
    void getRecentAttacks_shouldReturn200WithData() throws Exception {
        // Given
        List<AttackEventResponse> mockAttacks = List.of(
            new AttackEventResponse(
                "1",
                "2024-12-10T14:35:22",
                "192.168.1.100",
                "/api/users",
                "SQL_INJECTION",
                "high",
                true,
                "Mozilla/5.0",
                "SELECT * FROM users",
                List.of("SQL-001")
            ),
            new AttackEventResponse(
                "2",
                "2024-12-10T14:30:15",
                "192.168.1.101",
                "/api/login",
                "XSS",
                "medium",
                true,
                "Mozilla/5.0",
                "<script>alert('XSS')</script>",
                List.of("XSS-002")
            )
        );
        when(dashboardService.getRecentAttacks(anyInt())).thenReturn(mockAttacks);

        // When & Then
        mockMvc.perform(get("/api/dashboard/attacks").param("limit", "10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].id").value("1"))
            .andExpect(jsonPath("$.data[0].sourceIp").value("192.168.1.100"))
            .andExpect(jsonPath("$.data[0].targetPath").value("/api/users"))
            .andExpect(jsonPath("$.data[0].attackType").value("SQL_INJECTION"))
            .andExpect(jsonPath("$.data[0].severity").value("high"))
            .andExpect(jsonPath("$.data[0].blocked").value(true))
            .andExpect(jsonPath("$.data[0].matchedRules[0]").value("SQL-001"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/attacks - 잘못된 파라미터 (0)")
    void getRecentAttacks_withInvalidParameter_shouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/attacks").param("limit", "0"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/attacks - 잘못된 파라미터 (너무 큰 값)")
    void getRecentAttacks_withTooLargeParameter_shouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/dashboard/attacks").param("limit", "150"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/traffic - 기본 파라미터 (파라미터 없음)")
    void getTrafficData_withDefaultParameter_shouldReturn200() throws Exception {
        // Given
        when(dashboardService.getTrafficData(24)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/dashboard/traffic"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/attacks - 기본 파라미터 (파라미터 없음)")
    void getRecentAttacks_withDefaultParameter_shouldReturn200() throws Exception {
        // Given
        when(dashboardService.getRecentAttacks(10)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/dashboard/attacks"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }
}
