package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AuthUserArgumentResolverTest {

    private AuthUserArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new AuthUserArgumentResolver();
    }

    // ===== 테스트용 컨트롤러 =====
    static class TestController {

        public void valid(@Auth AuthUser authUser) {}

        public void noAnnotation(AuthUser authUser) {}

        public void wrongType(@Auth String wrong) {}

        public void neither(String str) {}
    }

    private MethodParameter getParameter(String methodName) throws Exception {
        Method method = TestController.class.getMethod(methodName, methodName.equals("wrongType") ? String.class :
                methodName.equals("neither") ? String.class : AuthUser.class);
        return new MethodParameter(method, 0);
    }

    @Test
    @DisplayName("정상 케이스")
    void supportsParameter_정상() throws Exception {
        MethodParameter parameter = getParameter("valid");

        boolean result = resolver.supportsParameter(parameter);

        assertTrue(result);
    }

    @Test
    @DisplayName("둘 다 없음 → false")
    void supportsParameter_둘다없으면_false() throws Exception {
        MethodParameter parameter = getParameter("neither");

        boolean result = resolver.supportsParameter(parameter);

        assertFalse(result);
    }

    @Test
    @DisplayName("어노테이션만 있음 → 예외")
    void supportsParameter_타입불일치_예외() throws Exception {
        MethodParameter parameter = getParameter("wrongType");

        assertThrows(AuthException.class,
                () -> resolver.supportsParameter(parameter));
    }

    @Test
    @DisplayName("resolveArgument 정상 동작")
    void resolveArgument_정상() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 1L);
        request.setAttribute("email", "test@test.com");
        request.setAttribute("userRole", "USER");

        NativeWebRequest webRequest = new ServletWebRequest(request);

        Object result = resolver.resolveArgument(
                null,
                null,
                webRequest,
                null
        );

        assertTrue(result instanceof AuthUser);

        AuthUser authUser = (AuthUser) result;

        assertEquals(1L, authUser.getId());
        assertEquals("test@test.com", authUser.getEmail());
        assertEquals(UserRole.USER, authUser.getUserRole());
    }
}