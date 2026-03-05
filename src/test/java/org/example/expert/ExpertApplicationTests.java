package org.example.expert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExpertApplicationTests {

    @Test
    @DisplayName("main 메서드 실행에 성공한다")
    void contextLoads() {
        ExpertApplication.main(new String[]{});
    }

}
