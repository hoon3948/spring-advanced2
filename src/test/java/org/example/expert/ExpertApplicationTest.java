package org.example.expert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExpertApplicationTest {

    @Test
    @DisplayName("main 메서드 실행에 성공한다")
    void main에_성공한다() {
        ExpertApplication.main(new String[]{});
    }
}