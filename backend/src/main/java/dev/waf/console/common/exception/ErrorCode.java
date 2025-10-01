package dev.waf.console.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 *
 * @author WAF Console Team
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors (1000-1999)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "C002", "입력값 검증에 실패했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C004", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C005", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C006", "서버 내부 오류가 발생했습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "C007", "서비스를 사용할 수 없습니다."),

    // Authentication Errors (2000-2999)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "토큰이 만료되었습니다."),
    GOOGLE_OAUTH_FAILED(HttpStatus.UNAUTHORIZED, "A003", "Google OAuth 인증에 실패했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "A004", "사용자를 찾을 수 없습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "A005", "권한이 부족합니다."),

    // WAF Rule Errors (3000-3999)
    RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "WAF 룰을 찾을 수 없습니다."),
    RULE_SYNTAX_ERROR(HttpStatus.BAD_REQUEST, "R002", "WAF 룰 문법 오류입니다."),
    RULE_DEPLOYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R003", "WAF 룰 배포에 실패했습니다."),
    RULE_ALREADY_EXISTS(HttpStatus.CONFLICT, "R004", "동일한 이름의 룰이 이미 존재합니다."),
    RULE_IN_USE(HttpStatus.CONFLICT, "R005", "사용 중인 룰은 삭제할 수 없습니다."),
    RULE_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "R006", "룰 유효성 검사에 실패했습니다."),

    // Log Errors (4000-4999)
    LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "로그를 찾을 수 없습니다."),
    LOG_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L002", "로그 검색에 실패했습니다."),
    LOG_EXPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "L003", "로그 내보내기에 실패했습니다."),

    // Simulation Errors (5000-5999)
    SIMULATION_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "시뮬레이션을 찾을 수 없습니다."),
    SIMULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "공격 시뮬레이션에 실패했습니다."),
    INVALID_TARGET_URL(HttpStatus.BAD_REQUEST, "S003", "유효하지 않은 대상 URL입니다."),
    DVWA_TEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S004", "DVWA 테스트에 실패했습니다."),
    SCREENSHOT_CAPTURE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S005", "스크린샷 캡처에 실패했습니다."),

    // Report Errors (6000-6999)
    REPORT_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P001", "리포트 생성에 실패했습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "리포트를 찾을 수 없습니다."),
    PDF_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P003", "PDF 생성에 실패했습니다."),

    // Whitelist Errors (7000-7999)
    WHITELIST_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "화이트리스트 항목을 찾을 수 없습니다."),
    INVALID_IP_FORMAT(HttpStatus.BAD_REQUEST, "W002", "유효하지 않은 IP 주소 형식입니다."),
    INVALID_DOMAIN_FORMAT(HttpStatus.BAD_REQUEST, "W003", "유효하지 않은 도메인 형식입니다."),
    WHITELIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "W004", "이미 등록된 화이트리스트 항목입니다."),

    // External Service Errors (8000-8999)
    MODSECURITY_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "ModSecurity API 오류입니다."),
    ELASTICSEARCH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E002", "Elasticsearch 오류입니다."),
    REDIS_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E003", "Redis 오류입니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E004", "데이터베이스 오류입니다."),
    WEBSOCKET_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E005", "WebSocket 연결 오류입니다."),

    // File Upload Errors (9000-9999)
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "F001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "F002", "지원하지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F003", "파일 크기가 제한을 초과했습니다."),
    BULK_IMPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F004", "대량 가져오기에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}