package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);

        String secret = "test-secret-key-test-secret-key-1234"; // 최소 256bit(32byte) --> 32byte 이상으로 변경
        secretKeyField.set(jwtUtil, Base64.getEncoder().encodeToString(secret.getBytes()));

        jwtUtil.init();
    }

    @Test
    @DisplayName("토큰 생성에 성공한다")
    void createToken에_성공한다() {

        Long userId = 1L;
        String email = "test@test.com";
        UserRole role = UserRole.USER;

        String token = jwtUtil.createToken(userId, email, role);

        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    @DisplayName("Bearer 토큰 substring에 성공한다")
    void substringToken에_성공한다() {

        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);

        String substring = jwtUtil.substringToken(token);

        assertFalse(substring.startsWith("Bearer "));
    }

    @Test
    @DisplayName("Bearer 토큰이 아니면 예외가 발생한다")
    void substringToken에_실패한다() {

        String invalidToken = "invalid-token";

        assertThrows(ServerException.class, () -> jwtUtil.substringToken(invalidToken));
    }

    @Test
    @DisplayName("Claims 추출에 성공한다")
    void extractClaims에_성공한다() {

        Long userId = 1L;
        String email = "test@test.com";
        UserRole role = UserRole.USER;

        String token = jwtUtil.createToken(userId, email, role);
        String substring = jwtUtil.substringToken(token);

        Claims claims = jwtUtil.extractClaims(substring);

        assertEquals("1", claims.getSubject());
        assertEquals(email, claims.get("email"));
    }

    @Test
    @DisplayName("substringToken 빈 문자열이면 예외 발생")
    void substringToken에_빈문자열이면_실패한다() {

        assertThrows(ServerException.class,
                () -> jwtUtil.substringToken(""));
    }
}