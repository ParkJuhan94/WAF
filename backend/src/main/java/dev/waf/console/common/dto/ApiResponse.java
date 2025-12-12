package dev.waf.console.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * API 공통 응답 형식
 *
 * @param <T> 응답 데이터 타입
 */
@Schema(description = "API 공통 응답 형식")
public record ApiResponse<T>(
    @Schema(description = "성공 여부", example = "true")
    boolean success,

    @Schema(description = "응답 데이터")
    T data,

    @Schema(description = "에러 메시지 (실패시)")
    String message,

    @Schema(description = "타임스탬프")
    String timestamp
) {
    /**
     * 성공 응답 생성
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now().toString());
    }

    /**
     * 에러 응답 생성
     *
     * @param message 에러 메시지
     * @param <T> 데이터 타입
     * @return 에러 응답
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, LocalDateTime.now().toString());
    }
}
