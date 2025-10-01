package dev.waf.console;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("백엔드 애플리케이션 단위 테스트")
class BackendApplicationTest {

    @Test
    @DisplayName("메인 애플리케이션 클래스가 존재한다")
    void mainApplicationClassExists() {
        // 메인 애플리케이션 클래스 존재 확인
        assert BackendApplication.class != null;
    }
}