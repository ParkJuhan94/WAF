package dev.waf.console.core.service;

import dev.waf.console.api.auth.dto.AuthResponse;
import dev.waf.console.core.domain.user.User;
import dev.waf.console.core.repository.UserRepository;
import dev.waf.console.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse authenticateWithGoogle(String code) {
        // 실제 구현에서는 Google API 호출하여 사용자 정보 획득
        // 지금은 더미 데이터로 처리
        String email = "test@example.com";
        String name = "Test User";
        String profileImage = "https://example.com/profile.jpg";
        String providerId = "google_123456";

        User user = userRepository.findByProviderAndProviderId("google", providerId)
                .orElseGet(() -> createNewUser(email, name, profileImage, providerId));

        user.updateLastLogin();
        userRepository.save(user);

        String accessToken = jwtTokenProvider.createToken(user.getId().toString(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(accessToken) // 실제로는 별도 refresh token
                .expiresIn(86400L)
                .userProfile(AuthResponse.UserProfile.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .name(user.getName())
                        .profileImage(user.getProfileImage())
                        .role(user.getRole())
                        .build())
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.createToken(userId, user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400L)
                .build();
    }

    public AuthResponse.UserProfile getCurrentUser(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.UserProfile.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .build();
    }

    private User createNewUser(String email, String name, String profileImage, String providerId) {
        User user = new User(email, name, profileImage, "google", providerId);
        return userRepository.save(user);
    }
}