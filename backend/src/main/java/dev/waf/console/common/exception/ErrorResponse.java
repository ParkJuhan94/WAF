package dev.waf.console.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 에러 응답 모델
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "R001", required = true)
    private final String code;

    @Schema(description = "에러 메시지", example = "WAF 룰을 찾을 수 없습니다.", required = true)
    private final String message;

    @Schema(description = "HTTP 상태 코드", example = "404", required = true)
    private final int status;

    @Schema(description = "요청 경로", example = "/api/v1/rules/123")
    private final String path;

    @Schema(description = "에러 발생 시간", example = "2024-09-29T15:30:45")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    @Schema(description = "상세 에러 정보 (개발 환경에서만 제공)")
    private final Object data;

    @Schema(description = "유효성 검증 에러 목록")
    private final List<ValidationError> validationErrors;

    @Schema(description = "추적 ID", example = "abc123def456")
    private final String traceId;

    /**
     * 유효성 검증 에러 정보
     */
    @Getter
    @Builder
    @Schema(description = "유효성 검증 에러")
    public static class ValidationError {

        @Schema(description = "필드명", example = "ruleName")
        private final String field;

        @Schema(description = "입력값", example = "")
        private final Object rejectedValue;

        @Schema(description = "에러 메시지", example = "룰 이름은 필수입니다.")
        private final String message;
    }

    /**
     * 기본 에러 응답 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 커스텀 메시지 에러 응답 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .status(errorCode.getHttpStatus().value())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 데이터 포함 에러 응답 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, Object data) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .path(path)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    /**
     * 유효성 검증 에러 응답 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String path, List<ValidationError> validationErrors) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .path(path)
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();
    }
}