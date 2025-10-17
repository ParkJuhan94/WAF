package dev.waf.console.common.fixture;

import dev.waf.console.user.domain.User;
import dev.waf.console.user.domain.UserRole;

import java.time.LocalDateTime;

/**
 * User 엔티티 테스트 픽스처
 *
 * 테스트에서 재사용 가능한 User 객체를 생성합니다.
 */
public class UserFixtures {

    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_NAME = "Test User";
    public static final String DEFAULT_PROVIDER = "google";
    public static final String DEFAULT_PROVIDER_ID = "google_123456";
    public static final String DEFAULT_PROFILE_IMAGE = "https://example.com/profile.jpg";

    /**
     * 기본 테스트 사용자 생성
     */
    public static User createDefaultUser() {
        return new User(
            DEFAULT_EMAIL,
            DEFAULT_NAME,
            DEFAULT_PROFILE_IMAGE,
            DEFAULT_PROVIDER,
            DEFAULT_PROVIDER_ID
        );
    }

    /**
     * 관리자 권한 사용자 생성
     */
    public static User createAdminUser() {
        User user = new User(
            "admin@example.com",
            "Admin User",
            DEFAULT_PROFILE_IMAGE,
            DEFAULT_PROVIDER,
            "google_admin_123"
        );
        // Note: UserRole을 변경하려면 User 엔티티에 setter 추가 필요
        return user;
    }

    /**
     * 커스텀 이메일 사용자 생성
     */
    public static User createUserWithEmail(String email) {
        return new User(
            email,
            DEFAULT_NAME,
            DEFAULT_PROFILE_IMAGE,
            DEFAULT_PROVIDER,
            DEFAULT_PROVIDER_ID
        );
    }

    /**
     * 완전히 커스터마이징된 사용자 생성
     */
    public static User createUser(String email, String name, String provider, String providerId) {
        return new User(email, name, DEFAULT_PROFILE_IMAGE, provider, providerId);
    }

    /**
     * 여러 사용자 생성
     */
    public static User[] createMultipleUsers(int count) {
        User[] users = new User[count];
        for (int i = 0; i < count; i++) {
            users[i] = new User(
                "user" + i + "@example.com",
                "User " + i,
                DEFAULT_PROFILE_IMAGE,
                DEFAULT_PROVIDER,
                DEFAULT_PROVIDER_ID + "_" + i
            );
        }
        return users;
    }

    /**
     * 마지막 로그인 시간이 오래된 사용자
     */
    public static User createInactiveUser() {
        User user = createDefaultUser();
        // 필요시 리플렉션으로 lastLoginAt 설정 가능
        return user;
    }

    /**
     * 특정 provider를 사용하는 사용자
     */
    public static User createUserWithProvider(String provider, String providerId) {
        return new User(
            DEFAULT_EMAIL,
            DEFAULT_NAME,
            DEFAULT_PROFILE_IMAGE,
            provider,
            providerId
        );
    }
}
