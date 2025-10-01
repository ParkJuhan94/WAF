package dev.waf.console.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 *
 * @RestControllerAdvice를 사용하여 모든 @RestController에서 발생하는 예외를 중앙집중식으로 처리
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
@Hidden // Swagger 문서에서 숨김
public class GlobalExceptionHandler {

    @Value("${app.debug:false}")
    private boolean debugMode;

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Business Exception [{}]: {} - {}", traceId, e.getErrorCode().getCode(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getErrorCode().getCode())
                .message(e.getMessage())
                .status(e.getErrorCode().getHttpStatus().value())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .data(debugMode ? e.getData() : null)
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    /**
     * Bean Validation 예외 처리 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Validation Exception [{}]: {}", traceId, e.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .rejectedValue(error.getRejectedValue())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message("입력값 검증에 실패했습니다.")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .validationErrors(validationErrors)
                .traceId(traceId)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 제약 조건 위반 예외 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Constraint Violation Exception [{}]: {}", traceId, e.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = e.getConstraintViolations()
                .stream()
                .map(violation -> ErrorResponse.ValidationError.builder()
                        .field(getFieldName(violation))
                        .rejectedValue(violation.getInvalidValue())
                        .message(violation.getMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.getCode())
                .message("제약 조건 위반입니다.")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .validationErrors(validationErrors)
                .traceId(traceId)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 인증 예외 처리
     */
    @ExceptionHandler({AuthenticationException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Authentication Exception [{}]: {}", traceId, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED, request.getRequestURI());
        errorResponse = ErrorResponse.builder()
                .code(errorResponse.getCode())
                .message(errorResponse.getMessage())
                .status(errorResponse.getStatus())
                .path(errorResponse.getPath())
                .timestamp(errorResponse.getTimestamp())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 접근 권한 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Access Denied Exception [{}]: {}", traceId, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FORBIDDEN, request.getRequestURI());
        errorResponse = ErrorResponse.builder()
                .code(errorResponse.getCode())
                .message(errorResponse.getMessage())
                .status(errorResponse.getStatus())
                .path(errorResponse.getPath())
                .timestamp(errorResponse.getTimestamp())
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * HTTP 메서드 미지원 예외 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Method Not Supported Exception [{}]: {}", traceId, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                String.format("지원하지 않는 HTTP 메서드입니다: %s", e.getMethod()),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * 핸들러를 찾을 수 없는 예외 처리 (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("No Handler Found Exception [{}]: {}", traceId, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 요청 파라미터 누락 예외 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Missing Parameter Exception [{}]: {}", traceId, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.VALIDATION_FAILED,
                String.format("필수 파라미터가 누락되었습니다: %s", e.getParameterName()),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 메서드 인자 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Argument Type Mismatch Exception [{}]: {}", traceId, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.VALIDATION_FAILED,
                String.format("파라미터 타입이 올바르지 않습니다: %s", e.getName()),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * HTTP 메시지 읽기 불가 예외 처리 (잘못된 JSON 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.warn("Message Not Readable Exception [{}]: {}", traceId, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_REQUEST,
                "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 모든 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e,
            HttpServletRequest request) {

        String traceId = generateTraceId();
        log.error("Unexpected Exception [{}]: ", traceId, e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message(debugMode ? e.getMessage() : ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .data(debugMode ? e.getClass().getSimpleName() : null)
                .traceId(traceId)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 제약 조건 위반에서 필드명 추출
     */
    private String getFieldName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String[] parts = propertyPath.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : propertyPath;
    }

    /**
     * 추적 ID 생성
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}