package dev.waf.console.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Google ID Token 로그인 요청")
public class GoogleLoginRequest {

    @NotBlank(message = "ID Token은 필수입니다")
    @Schema(
        description = "Google One Tap에서 받은 ID Token (JWT)",
        example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE5ZmUyYTdiNjc5NTIzOTYwNmNhMGE3NTA3OTRhN2JkN2IxOTc5YjgiLCJ0eXAiOiJKV1QifQ...",
        required = true
    )
    private String idToken;
}