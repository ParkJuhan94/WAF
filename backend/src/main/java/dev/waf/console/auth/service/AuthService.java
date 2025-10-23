package dev.waf.console.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dev.waf.console.auth.api.dto.AuthResponse;
import dev.waf.console.user.domain.User;
import dev.waf.console.user.repository.UserRepository;
import dev.waf.console.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Transactional
    public AuthResponse authenticateWithGoogle(String idToken) {
        try {
            // Google ID Token 검증
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new RuntimeException("Invalid ID token");
            }

            // ID Token에서 사용자 정보 추출
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String profileImage = (String) payload.get("picture");
            String providerId = payload.getSubject(); // Google User ID

            log.info("Google login success: email={}, name={}, sub={}", email, name, providerId);

            // 사용자 조회 또는 생성
            User user = userRepository.findByProviderAndProviderId("google", providerId)
                    .orElseGet(() -> createNewUser(email, name, profileImage, providerId));

            user.updateLastLogin();
            userRepository.save(user);

            // JWT 토큰 발급
            String accessToken = jwtTokenProvider.createToken(user.getId().toString(), user.getEmail());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(accessToken) // 실제로는 별도 refresh token
                    .expiresIn(3600L) // 1시간
                    .tokenType("Bearer")
                    .userProfile(AuthResponse.UserProfile.builder()
                            .id(user.getId().toString())
                            .email(user.getEmail())
                            .name(user.getName())
                            .profileImage(user.getProfileImage())
                            .role(user.getRole())
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Google ID token verification failed", e);
            throw new RuntimeException("Google 인증에 실패했습니다: " + e.getMessage());
        }
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