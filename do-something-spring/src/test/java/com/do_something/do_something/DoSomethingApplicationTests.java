package com.do_something.do_something;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Context 로딩 통합 테스트.
 * <p>실행 전 로컬 DB가 기동 중이고 .env 환경변수가 설정되어 있어야 한다.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class DoSomethingApplicationTests {

    @Test
    void contextLoads() {
        // Spring Context가 에러 없이 로딩되는지 검증
    }
}
