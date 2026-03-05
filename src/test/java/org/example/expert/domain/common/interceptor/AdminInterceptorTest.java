package org.example.expert.domain.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AdminInterceptorTest {

    private AdminInterceptor adminInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        adminInterceptor = new AdminInterceptor();
    }

    @Test
    @DisplayName("관리자 접근에 성공한다")
    void preHandle에_성공한다() throws Exception {
        // given
        Mockito.when(request.getAttribute("userRole")).thenReturn("ADMIN");
        Mockito.when(request.getRequestURI()).thenReturn("/admin/users");

        // when
        boolean result = adminInterceptor.preHandle(request, response, new Object());

        // then
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("관리자가 아니면 접근에 실패한다")
    void preHandle에_실패한다() throws Exception {
        // given
        Mockito.when(request.getAttribute("userRole")).thenReturn("USER");

        // when
        boolean result = adminInterceptor.preHandle(request, response, new Object());

        // then
        Assertions.assertFalse(result);
        Mockito.verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "접근권한이 없습니다.");
    }
}