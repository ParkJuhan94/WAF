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
        return new AuthResponse(
            DEFAULT_ACCESS_TOKEN,
            DEFAULT_REFRESH_TOKEN,
            DEFAULT_EXPIRES_IN,
            DEFAULT_TOKEN_TYPE,
            createDefaultUserProfile()
        );
    }

    /**
     * User 엔티티로부터 AuthResponse 생성
     */
    public static AuthResponse createAuthResponseFromUser(User user) {
        return new AuthResponse(
            DEFAULT_ACCESS_TOKEN,
            DEFAULT_REFRESH_TOKEN,
            DEFAULT_EXPIRES_IN,
            DEFAULT_TOKEN_TYPE,
            createUserProfileFromUser(user)
        );
    }

    /**
     * 커스텀 토큰으로 AuthResponse 생성
     */
    public static AuthResponse createAuthResponseWithTokens(String accessToken, String refreshToken) {
        return new AuthResponse(
            accessToken,
            refreshToken,
            DEFAULT_EXPIRES_IN,
            DEFAULT_TOKEN_TYPE,
            createDefaultUserProfile()
        );
    }

    /**
     * 기본 UserProfile 생성
     */
    public static AuthResponse.UserProfile createDefaultUserProfile() {
        return new AuthResponse.UserProfile(
            "1",
            UserFixtures.DEFAULT_EMAIL,
            UserFixtures.DEFAULT_NAME,
            UserFixtures.DEFAULT_PROFILE_IMAGE,
            UserRole.FREE_USER
        );
    }

    /**
     * 관리자 UserProfile 생성
     */
    public static AuthResponse.UserProfile createAdminUserProfile() {
        return new AuthResponse.UserProfile(
            "1",
            "admin@example.com",
            "Admin User",
            UserFixtures.DEFAULT_PROFILE_IMAGE,
            UserRole.ADMIN
        );
    }

    /**
     * User 엔티티로부터 UserProfile 생성
     */
    public static AuthResponse.UserProfile createUserProfileFromUser(User user) {
        return new AuthResponse.UserProfile(
            user.getId() != null ? user.getId().toString() : "1",
            user.getEmail(),
            user.getName(),
            user.getProfileImage(),
            user.getRole()
        );
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
        return new AuthResponse.UserProfile(
            id,
            email,
            name,
            UserFixtures.DEFAULT_PROFILE_IMAGE,
            role
        );
    }

    /**
     * 만료 시간이 짧은 AuthResponse 생성 (테스트용)
     */
    public static AuthResponse createShortLivedAuthResponse() {
        return new AuthResponse(
            DEFAULT_ACCESS_TOKEN,
            DEFAULT_REFRESH_TOKEN,
            60L, // 1분
            DEFAULT_TOKEN_TYPE,
            createDefaultUserProfile()
        );
    }

    /**
     * 만료 시간이 긴 AuthResponse 생성 (테스트용)
     */
    public static AuthResponse createLongLivedAuthResponse() {
        return new AuthResponse(
            DEFAULT_ACCESS_TOKEN,
            DEFAULT_REFRESH_TOKEN,
            86400L, // 24시간
            DEFAULT_TOKEN_TYPE,
            createDefaultUserProfile()
        );
    }

    /**
     * Refresh Token이 없는 AuthResponse 생성
     */
    public static AuthResponse createAuthResponseWithoutRefreshToken() {
        return new AuthResponse(
            DEFAULT_ACCESS_TOKEN,
            null,
            DEFAULT_EXPIRES_IN,
            DEFAULT_TOKEN_TYPE,
            createDefaultUserProfile()
        );
    }
}
