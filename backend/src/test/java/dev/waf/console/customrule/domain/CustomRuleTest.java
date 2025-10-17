package dev.waf.console.customrule.domain;

import dev.waf.console.common.fixture.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CustomRule 도메인 테스트
 *
 * TestFixtures를 사용한 도메인 로직 테스트 예시
 */
@DisplayName("CustomRule 도메인 테스트")
class CustomRuleTest implements TestFixtures {

    @Test
    @DisplayName("기본 커스텀 룰 생성")
    void createDefaultRule() {
        // given & when: TestFixture로 커스텀 룰 생성
        CustomRule rule = rules().createDefaultRule();

        // then: 생성된 룰 검증
        assertThat(rule).isNotNull();
        assertThat(rule.getName()).isNotBlank();
        assertThat(rule.getEnabled()).isTrue();
        assertThat(rule.getMatchCount()).isZero();
        assertThat(rule.getBlockCount()).isZero();
    }

    @Test
    @DisplayName("룰 활성화/비활성화 테스트")
    void enableDisableRule() {
        // given: 기본 룰 생성
        CustomRule rule = rules().createDefaultRule();
        assertThat(rule.getEnabled()).isTrue();

        // when: 룰 비활성화
        rule.disable();

        // then: 비활성화 상태 확인
        assertThat(rule.getEnabled()).isFalse();

        // when: 룰 재활성화
        rule.enable();

        // then: 활성화 상태 확인
        assertThat(rule.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("룰 매치 카운트 기록")
    void recordRuleMatch() {
        // given: 기본 룰 생성
        CustomRule rule = rules().createDefaultRule();
        assertThat(rule.getMatchCount()).isZero();

        // when: 매치 기록
        rule.recordMatch();
        rule.recordMatch();
        rule.recordMatch();

        // then: 매치 카운트 증가 확인
        assertThat(rule.getMatchCount()).isEqualTo(3);
        assertThat(rule.getLastMatchedAt()).isNotNull();
    }

    @Test
    @DisplayName("룰 차단 카운트 기록")
    void recordRuleBlock() {
        // given: 기본 룰 생성
        CustomRule rule = rules().createDefaultRule();
        assertThat(rule.getBlockCount()).isZero();

        // when: 차단 기록
        rule.recordBlock();
        rule.recordBlock();

        // then: 차단 카운트 증가 확인
        assertThat(rule.getBlockCount()).isEqualTo(2);
        assertThat(rule.getLastMatchedAt()).isNotNull();
    }

    @Test
    @DisplayName("타겟 스코프 설정")
    void setTargetScope() {
        // given: 기본 룰 생성
        CustomRule rule = rules().createDefaultRule();

        // when: 타겟 스코프 설정
        String targetService = "api-gateway";
        String targetPath = "/api/v1/*";
        rule.setTargetScope(targetService, targetPath);

        // then: 스코프 설정 확인
        assertThat(rule.getTargetService()).isEqualTo(targetService);
        assertThat(rule.getTargetPath()).isEqualTo(targetPath);
    }

    @Test
    @DisplayName("XSS 차단 룰 생성 및 검증")
    void createXssRule() {
        // given & when: XSS 차단 룰 생성
        CustomRule xssRule = rules().createXssRule();

        // then: XSS 룰 검증
        assertThat(xssRule.getType()).isEqualTo(RuleType.BLOCK);
        assertThat(xssRule.getSeverity()).isEqualTo(RuleSeverity.HIGH);
        assertThat(xssRule.getName()).contains("XSS");
    }
}
