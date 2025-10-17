package dev.waf.console.common.fixture;

import dev.waf.console.auth.api.dto.AuthResponse;
import dev.waf.console.user.domain.User;
import dev.waf.console.user.domain.UserRole;

/**
 * Auth 관련 DTO 테스트 픽스처
 *
 * 테스트에서 재사용 가능한 Auth 관련 객체를 생성합니다.
 */
public class AuthFixtures {

    public static final String DEFAULT_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.access.token";
    public static final String DEFAULT_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.refresh.token";
    public static final Long DEFAULT_EXPIRES_IN = 3600L;
    public static final String DEFAULT_TOKEN_TYPE = "Bearer";

    /**
     * 기본 AuthResponse 생성
     */
    public static AuthResponse createDefaultAuthResponse() {
        return AuthResponse.builder()
            .accessToken(DEFAULT_ACCESS_TOKEN)
            .refreshToken(DEFAULT_REFRESH_TOKEN)
            .expiresIn(DEFAULT_EXPIRES_IN)
            .tokenType(DEFAULT_TOKEN_TYPE)
            .userProfile(createDefaultUserProfile())
            .build();
    }

    /**
     * User 엔티티로부터 AuthResponse 생성
     */
    public static AuthResponse createAuthResponseFromUser(User user) {
        return AuthResponse.builder()
            .accessToken(DEFAULT_ACCESS_TOKEN)
            .refreshToken(DEFAULT_REFRESH_TOKEN)
            .expiresIn(DEFAULT_EXPIRES_IN)
            .tokenType(DEFAULT_TOKEN_TYPE)
            .userProfile(createUserProfileFromUser(user))
            .build();
    }

    /**
     * 커스텀 토큰으로 AuthResponse 생성
     */
    public static AuthResponse createAuthResponseWithTokens(String accessToken, String refreshToken) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(DEFAULT_EXPIRES_IN)
            .tokenType(DEFAULT_TOKEN_TYPE)
            .userProfile(createDefaultUserProfile())
            .build();
    }

    /**
     * 기본 UserProfile 생성
     */
    public static AuthResponse.UserProfile createDefaultUserProfile() {
        return AuthResponse.UserProfile.builder()
            .id("1")
            .email(UserFixtures.DEFAULT_EMAIL)
            .name(UserFixtures.DEFAULT_NAME)
            .profileImage(UserFixtures.DEFAULT_PROFILE_IMAGE)
            .role(UserRole.FREE_USER)
            .build();
    }

    /**
     * 관리자 UserProfile 생성
     */
    public static AuthResponse.UserProfile createAdminUserProfile() {
        return AuthResponse.UserProfile.builder()
            .id("1")
            .email("admin@example.com")
            .name("Admin User")
            .profileImage(UserFixtures.DEFAULT_PROFILE_IMAGE)
            .role(UserRole.ADMIN)
            .build();
    }

    /**
     * User 엔티티로부터 UserProfile 생성
     */
    public static AuthResponse.UserProfile createUserProfileFromUser(User user) {
        return AuthResponse.UserProfile.builder()
            .id(user.getId() != null ? user.getId().toString() : "1")
            .email(user.getEmail())
            .name(user.getName())
            .profileImage(user.getProfileImage())
            .role(user.getRole())
            .build();
    }

    /**
     * 커스텀 UserProfile 생성
     */
    public static AuthResponse.UserProfile createUserProfile(
            String id,
            String email,
            String name,
            UserRole role
    ) {
        return AuthResponse.UserProfile.builder()
            .id(id)
            .email(email)
            .name(name)
            .profileImage(UserFixtures.DEFAULT_PROFILE_IMAGE)
            .role(role)
            .build();
    }

    /**
     * 만료 시간이 짧은 AuthResponse 생성 (테스트용)
     */
    public static AuthResponse createShortLivedAuthResponse() {
        return AuthResponse.builder()
            .accessToken(DEFAULT_ACCESS_TOKEN)
            .refreshToken(DEFAULT_REFRESH_TOKEN)
            .expiresIn(60L) // 1분
            .tokenType(DEFAULT_TOKEN_TYPE)
            .userProfile(createDefaultUserProfile())
            .build();
    }

    /**
     * 만료 시간이 긴 AuthResponse 생성 (테스트용)
     */
    public static AuthResponse createLongLivedAuthResponse() {
        return AuthResponse.builder()
            .accessToken(DEFAULT_ACCESS_TOKEN)
            .refreshToken(DEFAULT_REFRESH_TOKEN)
            .expiresIn(86400L) // 24시간
            .tokenType(DEFAULT_TOKEN_TYPE)
            .userProfile(createDefaultUserProfile())
            .build();
    }

    /**
     * Refresh Token이 없는 AuthResponse 생성
     */
    public static AuthResponse createAuthResponseWithoutRefreshToken() {
        return AuthResponse.builder()
            .accessToken(DEFAULT_ACCESS_TOKEN)
            .refreshToken(null)
            .expiresIn(DEFAULT_EXPIRES_IN)
            .tokenType(DEFAULT_TOKEN_TYPE)
            .userProfile(createDefaultUserProfile())
            .build();
    }
}
