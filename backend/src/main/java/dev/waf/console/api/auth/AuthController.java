package dev.waf.console.api.auth;

import dev.waf.console.api.auth.dto.AuthResponse;
import dev.waf.console.api.auth.dto.GoogleLoginRequest;
import dev.waf.console.common.exception.ErrorResponse;
import dev.waf.console.core.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "🔐 Authentication API",
    description = """
        ## Google OAuth 2.0 기반 인증 시스템

        ### 📋 주요 기능
        - **Google 소셜 로그인** - OAuth 2.0 Authorization Code 플로우
        - **JWT 토큰 관리** - Access Token + Refresh Token 방식
        - **사용자 정보 조회** - 현재 로그인한 사용자 프로필
        - **토큰 갱신** - Refresh Token을 통한 Access Token 재발급

        ### 🔄 인증 플로우
        1. **프론트엔드**: Google OAuth 인증 후 Authorization Code 획득
        2. **백엔드**: `/google` 엔드포인트로 Code 전송
        3. **백엔드**: Google API로 사용자 정보 조회 + JWT 토큰 발급
        4. **프론트엔드**: Access Token으로 API 호출
        5. **토큰 만료시**: `/refresh` 엔드포인트로 토큰 갱신

        ### 🛡️ 보안 정책
        - **Access Token**: 1시간 유효 (API 인증용)
        - **Refresh Token**: 30일 유효 (토큰 갱신용)
        - **멀티테넌트**: 사용자별 데이터 격리
        - **역할 기반 권한**: FREE_USER, PREMIUM_USER, ADMIN
        """
)
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Google OAuth 로그인",
        description = """
            Google OAuth 2.0 Authorization Code를 사용하여 사용자 인증을 수행합니다.

            ### 📋 처리 과정
            1. Google OAuth Authorization Code 검증
            2. Google API를 통한 사용자 정보 조회
            3. 신규 사용자인 경우 자동 회원가입
            4. JWT Access Token + Refresh Token 발급
            5. 사용자 프로필 정보 반환

            ### ⚠️ 주의사항
            - Authorization Code는 일회용이며, 10분 내에 사용해야 합니다
            - 유효하지 않은 Code 사용시 401 에러가 발생합니다
            - 최초 로그인시 FREE_USER 권한으로 자동 등록됩니다
            """,
        tags = {"🔐 Authentication API"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 로그인 성공",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "로그인 성공 응답",
                    summary = "Google OAuth 로그인 성공시 반환되는 응답",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "expiresIn": 3600,
                          "tokenType": "Bearer",
                          "userProfile": {
                            "id": "user-123456",
                            "email": "user@example.com",
                            "name": "홍길동",
                            "profileImage": "https://lh3.googleusercontent.com/...",
                            "role": "FREE_USER",
                            "createdAt": "2024-09-29T10:30:00",
                            "lastLoginAt": "2024-09-29T15:45:00"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ 잘못된 요청",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 인증 코드",
                    value = """
                        {
                          "code": "A003",
                          "message": "Google OAuth 인증에 실패했습니다.",
                          "status": 400,
                          "path": "/api/v1/auth/google",
                          "timestamp": "2024-09-29T15:30:45",
                          "traceId": "abc123def456"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "🚫 인증 실패",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "💥 서버 내부 오류",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
        @Parameter(
            name = "request",
            description = "Google OAuth Authorization Code 요청",
            required = true,
            schema = @Schema(implementation = GoogleLoginRequest.class)
        )
        @Valid @RequestBody GoogleLoginRequest request
    ) {
        AuthResponse response = authService.authenticateWithGoogle(request.getCode());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "JWT 토큰 갱신",
        description = """
            Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

            ### 📋 사용 시나리오
            - Access Token이 만료되었을 때 (401 에러 발생시)
            - 토큰 만료 전 사전 갱신 (보안 강화)

            ### 🔄 갱신 정책
            - **새 Access Token**: 1시간 유효
            - **새 Refresh Token**: 30일 유효 (기존 토큰 무효화)
            - **동시 갱신**: 두 토큰 모두 새로 발급

            ### ⚠️ 주의사항
            - Authorization 헤더에 `Bearer {refresh_token}` 형식으로 전송
            - 만료되거나 유효하지 않은 Refresh Token 사용시 401 에러
            - 토큰 갱신 후 기존 Refresh Token은 즉시 무효화됩니다
            """,
        tags = {"🔐 Authentication API"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 토큰 갱신 성공",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "🚫 유효하지 않은 Refresh Token",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
        @Parameter(
            name = "Authorization",
            description = "Refresh Token (Bearer {token} 형식)",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            schema = @Schema(type = "string", pattern = "^Bearer .+")
        )
        @RequestHeader("Authorization") String refreshToken
    ) {
        AuthResponse response = authService.refreshToken(refreshToken.substring(7)); // Remove "Bearer "
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = """
            현재 로그인한 사용자의 프로필 정보를 조회합니다.

            ### 📋 반환 정보
            - **기본 정보**: ID, 이메일, 이름, 프로필 이미지
            - **권한 정보**: 사용자 역할 (FREE_USER, PREMIUM_USER, ADMIN)
            - **활동 정보**: 가입일, 마지막 로그인 시간
            - **구독 정보**: 현재 구독 플랜 (SaaS)

            ### 🔐 권한 요구사항
            - 유효한 Access Token 필요
            - 모든 인증된 사용자 접근 가능

            ### 💡 활용 예시
            - 헤더 사용자 프로필 표시
            - 권한 기반 UI 제어
            - 사용자별 설정 로드
            """,
        tags = {"🔐 Authentication API"},
        security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "✅ 사용자 정보 조회 성공",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.UserProfile.class),
                examples = @ExampleObject(
                    name = "사용자 프로필",
                    value = """
                        {
                          "id": "user-123456",
                          "email": "user@example.com",
                          "name": "홍길동",
                          "profileImage": "https://lh3.googleusercontent.com/...",
                          "role": "PREMIUM_USER",
                          "subscription": "premium",
                          "createdAt": "2024-09-01T10:00:00",
                          "lastLoginAt": "2024-09-29T15:45:00",
                          "permissions": ["RULE_READ", "RULE_WRITE", "LOG_EXPORT"]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "🚫 인증되지 않은 요청",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "👤 사용자를 찾을 수 없음",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserProfile> getCurrentUser(
        @Parameter(
            name = "Authorization",
            description = "Access Token (Bearer {token} 형식)",
            required = true,
            example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            schema = @Schema(type = "string", pattern = "^Bearer .+")
        )
        @RequestHeader("Authorization") String token
    ) {
        AuthResponse.UserProfile user = authService.getCurrentUser(token.substring(7));
        return ResponseEntity.ok(user);
    }
}