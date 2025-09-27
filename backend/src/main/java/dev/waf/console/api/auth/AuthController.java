package dev.waf.console.api.auth;

import dev.waf.console.api.auth.dto.AuthResponse;
import dev.waf.console.api.auth.dto.GoogleLoginRequest;
import dev.waf.console.core.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.authenticateWithGoogle(request.getCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken.substring(7)); // Remove "Bearer "
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserProfile> getCurrentUser(@RequestHeader("Authorization") String token) {
        AuthResponse.UserProfile user = authService.getCurrentUser(token.substring(7));
        return ResponseEntity.ok(user);
    }
}