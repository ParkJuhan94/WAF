package dev.waf.console.auth.api.dto;

import dev.waf.console.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답")
public record AuthResponse(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIs...")
    String accessToken,

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIs...")
    String refreshToken,

    @Schema(description = "토큰 만료 시간 (초)", example = "3600")
    Long expiresIn,

    @Schema(description = "토큰 타입", example = "Bearer")
    String tokenType,

    @Schema(description = "사용자 프로필 정보")
    UserProfile userProfile
) {
    public record UserProfile(
        @Schema(description = "사용자 ID", example = "1")
        String id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "프로필 이미지 URL")
        String profileImage,

        @Schema(description = "사용자 역할", example = "FREE_USER")
        UserRole role
    ) {
    }
}
