package dev.waf.console.common.fixture;

/**
 * 모든 테스트 픽스처에 대한 통합 접근 인터페이스
 *
 * 테스트 코드에서 픽스처를 쉽게 사용할 수 있도록 하는 인터페이스입니다.
 * 테스트 클래스에서 이 인터페이스를 구현하면 모든 픽스처 메서드를 직접 호출할 수 있습니다.
 *
 * 사용 예시:
 * <pre>
 * public class UserServiceTest implements TestFixtures {
 *
 *     @Test
 *     void testCreateUser() {
 *         // 픽스처를 직접 사용
 *         User user = users().createDefaultUser();
 *         CustomRule rule = rules().createDefaultRule();
 *         WAFLog log = logs().createBlockedLog();
 *         AuthResponse auth = auth().createDefaultAuthResponse();
 *     }
 * }
 * </pre>
 */
public interface TestFixtures {

    /**
     * User 픽스처 접근
     */
    default UserFixtureBuilder users() {
        return new UserFixtureBuilder();
    }

    /**
     * CustomRule 픽스처 접근
     */
    default CustomRuleFixtureBuilder rules() {
        return new CustomRuleFixtureBuilder();
    }

    /**
     * WAFLog 픽스처 접근
     */
    default WAFLogFixtureBuilder logs() {
        return new WAFLogFixtureBuilder();
    }

    /**
     * Auth 픽스처 접근
     */
    default AuthFixtureBuilder auth() {
        return new AuthFixtureBuilder();
    }

    /**
     * User 픽스처 빌더
     */
    class UserFixtureBuilder {
        public dev.waf.console.user.domain.User createDefaultUser() {
            return UserFixtures.createDefaultUser();
        }

        public dev.waf.console.user.domain.User createAdminUser() {
            return UserFixtures.createAdminUser();
        }

        public dev.waf.console.user.domain.User createUserWithEmail(String email) {
            return UserFixtures.createUserWithEmail(email);
        }

        public dev.waf.console.user.domain.User createUser(String email, String name, String provider, String providerId) {
            return UserFixtures.createUser(email, name, provider, providerId);
        }

        public dev.waf.console.user.domain.User[] createMultipleUsers(int count) {
            return UserFixtures.createMultipleUsers(count);
        }
    }

    /**
     * CustomRule 픽스처 빌더
     */
    class CustomRuleFixtureBuilder {
        public dev.waf.console.customrule.domain.CustomRule createDefaultRule() {
            return CustomRuleFixtures.createDefaultRule();
        }

        public dev.waf.console.customrule.domain.CustomRule createXssRule() {
            return CustomRuleFixtures.createXssRule();
        }

        public dev.waf.console.customrule.domain.CustomRule createPathTraversalRule() {
            return CustomRuleFixtures.createPathTraversalRule();
        }

        public dev.waf.console.customrule.domain.CustomRule createDisabledRule() {
            return CustomRuleFixtures.createDisabledRule();
        }

        public dev.waf.console.customrule.domain.CustomRule[] createMultipleRules(int count) {
            return CustomRuleFixtures.createMultipleRules(count);
        }
    }

    /**
     * WAFLog 픽스처 빌더
     */
    class WAFLogFixtureBuilder {
        public dev.waf.console.waflog.domain.WAFLog createDefaultLog() {
            return WAFLogFixtures.createDefaultLog();
        }

        public dev.waf.console.waflog.domain.WAFLog createBlockedLog() {
            return WAFLogFixtures.createBlockedLog();
        }

        public dev.waf.console.waflog.domain.WAFLog createXssLog() {
            return WAFLogFixtures.createXssLog();
        }

        public dev.waf.console.waflog.domain.WAFLog createSqlInjectionLog() {
            return WAFLogFixtures.createSqlInjectionLog();
        }

        public dev.waf.console.waflog.domain.WAFLog createHighRiskLog() {
            return WAFLogFixtures.createHighRiskLog();
        }

        public dev.waf.console.waflog.domain.WAFLog[] createMultipleLogs(int count) {
            return WAFLogFixtures.createMultipleLogs(count);
        }
    }

    /**
     * Auth 픽스처 빌더
     */
    class AuthFixtureBuilder {
        public dev.waf.console.auth.api.dto.AuthResponse createDefaultAuthResponse() {
            return AuthFixtures.createDefaultAuthResponse();
        }

        public dev.waf.console.auth.api.dto.AuthResponse createAuthResponseFromUser(dev.waf.console.user.domain.User user) {
            return AuthFixtures.createAuthResponseFromUser(user);
        }

        public dev.waf.console.auth.api.dto.AuthResponse.UserProfile createDefaultUserProfile() {
            return AuthFixtures.createDefaultUserProfile();
        }

        public dev.waf.console.auth.api.dto.AuthResponse.UserProfile createAdminUserProfile() {
            return AuthFixtures.createAdminUserProfile();
        }
    }
}
