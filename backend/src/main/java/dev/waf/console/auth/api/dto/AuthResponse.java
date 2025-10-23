package dev.waf.console.auth.api.dto;

import dev.waf.console.user.domain.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private UserProfile userProfile;

    @Data
    @Builder
    public static class UserProfile {
        private String id;
        private String email;
        private String name;
        private String profileImage;
        private UserRole role;
    }
}