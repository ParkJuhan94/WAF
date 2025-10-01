package dev.waf.console.customrule.service;

import static dev.waf.console.customrule.domain.RuleType.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import dev.waf.console.customrule.domain.CustomRule;
import dev.waf.console.customrule.domain.RuleSeverity;
import dev.waf.console.customrule.repository.CustomRuleRepository;
import dev.waf.console.customrule.service.CustomRuleService;
import dev.waf.console.user.domain.User;
import dev.waf.console.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomRuleService 테스트")
class CustomRuleServiceTest {

    @Mock
    private CustomRuleRepository customRuleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomRuleService customRuleService;

    private User testUser;
    private CustomRule testRule;

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
                BLOCK,
                RuleSeverity.HIGH,
                testUser
        );
        ReflectionTestUtils.setField(testRule, "id", 1L);
    }

    @Test
    @DisplayName("룰 생성 성공")
    void createRule_Success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(customRuleRepository.existsByNameAndCreatedBy(anyString(), any(User.class)))
                .willReturn(false);
        given(customRuleRepository.save(any(CustomRule.class))).willReturn(testRule);

        // when
        CustomRule result = customRuleService.createRule(
                "Test SQL Injection Rule",
                "Test rule for SQL injection detection",
                "SecRule ARGS \"@detectSQLi\" \"id:1001,phase:2,block\"",
                BLOCK,
                RuleSeverity.HIGH,
                1L
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test SQL Injection Rule");
        assertThat(result.getType()).isEqualTo(BLOCK);
        assertThat(result.getSeverity()).isEqualTo(RuleSeverity.HIGH);
        assertThat(result.getCreatedBy()).isEqualTo(testUser);

        verify(userRepository).findById(1L);
        verify(customRuleRepository).existsByNameAndCreatedBy("Test SQL Injection Rule", testUser);
        verify(customRuleRepository).save(any(CustomRule.class));
    }

    @Test
    @DisplayName("룰 생성 실패 - 사용자 없음")
    void createRule_Fail_UserNotFound() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customRuleService.createRule(
                "Test Rule",
                "Test Description",
                "Test Content",
                BLOCK,
                RuleSeverity.HIGH,
                1L
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("사용자를 찾을 수 없습니다: 1");

        verify(userRepository).findById(1L);
        verify(customRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("룰 생성 실패 - 중복 이름")
    void createRule_Fail_DuplicateName() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(customRuleRepository.existsByNameAndCreatedBy("Test Rule", testUser))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> customRuleService.createRule(
                "Test Rule",
                "Test Description",
                "Test Content",
                BLOCK,
                RuleSeverity.HIGH,
                1L
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("이미 존재하는 룰 이름입니다: Test Rule");

        verify(userRepository).findById(1L);
        verify(customRuleRepository).existsByNameAndCreatedBy("Test Rule", testUser);
        verify(customRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("룰 조회 성공")
    void getRule_Success() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));

        // when
        CustomRule result = customRuleService.getRule(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test SQL Injection Rule");

        verify(customRuleRepository).findById(1L);
    }

    @Test
    @DisplayName("룰 조회 실패 - 룰 없음")
    void getRule_Fail_NotFound() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customRuleService.getRule(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("룰을 찾을 수 없습니다: 1");

        verify(customRuleRepository).findById(1L);
    }

    @Test
    @DisplayName("룰 수정 성공")
    void updateRule_Success() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(customRuleRepository.existsByNameAndCreatedBy(anyString(), any(User.class)))
                .willReturn(false);
        given(customRuleRepository.save(any(CustomRule.class))).willReturn(testRule);

        // when
        CustomRule result = customRuleService.updateRule(
                1L,
                "Updated Rule Name",
                "Updated Description",
                "Updated Content",
                DENY,
                RuleSeverity.CRITICAL,
                800,
                1L
        );

        // then
        assertThat(result).isNotNull();
        verify(customRuleRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(customRuleRepository).save(any(CustomRule.class));
    }

    @Test
    @DisplayName("룰 수정 실패 - 권한 없음")
    void updateRule_Fail_NoPermission() {
        // given
        User otherUser = new User(
                "other@example.com",
                "Other User",
                "https://example.com/other.jpg",
                "google",
                "google-456"
        );
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));
        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));

        // when & then
        assertThatThrownBy(() -> customRuleService.updateRule(
                1L,
                "Updated Rule Name",
                "Updated Description",
                "Updated Content",
                DENY,
                RuleSeverity.CRITICAL,
                800,
                2L
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("본인이 생성한 룰만 수정할 수 있습니다.");

        verify(customRuleRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(customRuleRepository, never()).save(any());
    }

    @Test
    @DisplayName("룰 삭제 성공")
    void deleteRule_Success() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));

        // when
        customRuleService.deleteRule(1L, 1L);

        // then
        verify(customRuleRepository).findById(1L);
        verify(customRuleRepository).delete(testRule);
    }

    @Test
    @DisplayName("룰 삭제 실패 - 권한 없음")
    void deleteRule_Fail_NoPermission() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));

        // when & then
        assertThatThrownBy(() -> customRuleService.deleteRule(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 생성한 룰만 삭제할 수 있습니다.");

        verify(customRuleRepository).findById(1L);
        verify(customRuleRepository, never()).delete(any());
    }

    @Test
    @DisplayName("룰 상태 토글 성공")
    void toggleRuleStatus_Success() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));
        given(customRuleRepository.save(any(CustomRule.class))).willReturn(testRule);

        // when
        CustomRule result = customRuleService.toggleRuleStatus(1L, 1L);

        // then
        assertThat(result).isNotNull();
        verify(customRuleRepository).findById(1L);
        verify(customRuleRepository).save(testRule);
    }

    @Test
    @DisplayName("사용자별 룰 조회 성공")
    void getUserRules_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomRule> rulePage = new PageImpl<>(List.of(testRule));

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(customRuleRepository.findByCreatedBy(testUser, pageable)).willReturn(rulePage);

        // when
        Page<CustomRule> result = customRuleService.getUserRules(1L, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testRule);

        verify(userRepository).findById(1L);
        verify(customRuleRepository).findByCreatedBy(testUser, pageable);
    }

    @Test
    @DisplayName("활성 룰 조회 성공")
    void getActiveRulesByPriority_Success() {
        // given
        List<CustomRule> activeRules = List.of(testRule);
        given(customRuleRepository.findByEnabledTrueOrderByPriorityDesc()).willReturn(activeRules);

        // when
        List<CustomRule> result = customRuleService.getActiveRulesByPriority();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRule);

        verify(customRuleRepository).findByEnabledTrueOrderByPriorityDesc();
    }

    @Test
    @DisplayName("룰 매치 기록 성공")
    void recordRuleMatch_Success() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));

        // when
        customRuleService.recordRuleMatch(1L);

        // then
        verify(customRuleRepository).findById(1L);
        verify(customRuleRepository).save(testRule);
    }

    @Test
    @DisplayName("룰 차단 기록 성공")
    void recordRuleBlock_Success() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));

        // when
        customRuleService.recordRuleBlock(1L);

        // then
        verify(customRuleRepository).findById(1L);
        verify(customRuleRepository).save(testRule);
    }

    @Test
    @DisplayName("룰 통계 조회 성공")
    void getRuleStatistics_Success() {
        // given
        given(customRuleRepository.count()).willReturn(10L);
        given(customRuleRepository.countByEnabled(true)).willReturn(8L);
        given(customRuleRepository.countByEnabled(false)).willReturn(2L);
        given(customRuleRepository.getTotalMatchCount()).willReturn(100L);
        given(customRuleRepository.getTotalBlockCount()).willReturn(50L);

        // when
        CustomRuleService.RuleStatistics result = customRuleService.getRuleStatistics();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRules()).isEqualTo(10L);
        assertThat(result.getEnabledRules()).isEqualTo(8L);
        assertThat(result.getDisabledRules()).isEqualTo(2L);
        assertThat(result.getTotalMatches()).isEqualTo(100L);
        assertThat(result.getTotalBlocks()).isEqualTo(50L);

        verify(customRuleRepository).count();
        verify(customRuleRepository).countByEnabled(true);
        verify(customRuleRepository).countByEnabled(false);
        verify(customRuleRepository).getTotalMatchCount();
        verify(customRuleRepository).getTotalBlockCount();
    }

    @Test
    @DisplayName("룰 통계 조회 성공 - null 값 처리")
    void getRuleStatistics_Success_WithNullValues() {
        // given
        given(customRuleRepository.count()).willReturn(5L);
        given(customRuleRepository.countByEnabled(true)).willReturn(3L);
        given(customRuleRepository.countByEnabled(false)).willReturn(2L);
        given(customRuleRepository.getTotalMatchCount()).willReturn(null);
        given(customRuleRepository.getTotalBlockCount()).willReturn(null);

        // when
        CustomRuleService.RuleStatistics result = customRuleService.getRuleStatistics();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRules()).isEqualTo(5L);
        assertThat(result.getEnabledRules()).isEqualTo(3L);
        assertThat(result.getDisabledRules()).isEqualTo(2L);
        assertThat(result.getTotalMatches()).isEqualTo(0L);
        assertThat(result.getTotalBlocks()).isEqualTo(0L);
    }

    @Test
    @DisplayName("특정 서비스 적용 가능한 룰 조회 성공")
    void getApplicableRules_Success() {
        // given
        String targetService = "user-service";
        List<CustomRule> applicableRules = List.of(testRule);
        given(customRuleRepository.findApplicableRules(targetService)).willReturn(applicableRules);

        // when
        List<CustomRule> result = customRuleService.getApplicableRules(targetService);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testRule);

        verify(customRuleRepository).findApplicableRules(targetService);
    }

    @Test
    @DisplayName("최근 활성 룰 조회 성공")
    void getRecentlyActiveRules_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomRule> recentRules = new PageImpl<>(List.of(testRule));
        given(customRuleRepository.findRecentlyActiveRules(pageable)).willReturn(recentRules);

        // when
        Page<CustomRule> result = customRuleService.getRecentlyActiveRules(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testRule);

        verify(customRuleRepository).findRecentlyActiveRules(pageable);
    }

    @Test
    @DisplayName("룰 대상 범위 설정 성공")
    void setRuleTargetScope_Success() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));
        given(customRuleRepository.save(any(CustomRule.class))).willReturn(testRule);

        // when
        CustomRule result = customRuleService.setRuleTargetScope(1L, "api-service", "/api/v1/*", 1L);

        // then
        assertThat(result).isNotNull();
        verify(customRuleRepository).findById(1L);
        verify(customRuleRepository).save(testRule);
    }

    @Test
    @DisplayName("룰 대상 범위 설정 실패 - 권한 없음")
    void setRuleTargetScope_Fail_NoPermission() {
        // given
        given(customRuleRepository.findById(1L)).willReturn(Optional.of(testRule));

        // when & then
        assertThatThrownBy(() -> customRuleService.setRuleTargetScope(1L, "api-service", "/api/v1/*", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인이 생성한 룰만 수정할 수 있습니다.");

        verify(customRuleRepository).findById(1L);
        verify(customRuleRepository, never()).save(any());
    }
}