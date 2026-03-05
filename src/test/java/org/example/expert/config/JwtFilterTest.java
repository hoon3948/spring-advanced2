package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(jwtUtil, objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("auth 경로는 필터를 통과한다")
    void doFilter에_성공한다_auth경로() throws Exception {

        request.setRequestURI("/auth/signin");

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증이 필요합니다")
    void doFilter에_인증헤더가_없어_실패한다() throws Exception {

        request.setRequestURI("/todos");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("정상 JWT이면 필터를 통과한다")
    void doFilter에_성공한다() throws Exception {

        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.substringToken(any())).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(claims);

        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("test@test.com");
        when(claims.get("userRole", String.class)).thenReturn("USER");

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        assertEquals(1L, request.getAttribute("userId"));
        assertEquals("test@test.com", request.getAttribute("email"));
        assertEquals("USER", request.getAttribute("userRole"));
    }

    @Test
    @DisplayName("관리자 권한이 없으면 접근이 거부된다")
    void doFilter에_admin권한이_없어_실패한다() throws Exception {

        request.setRequestURI("/admin/test");
        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.substringToken(any())).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(claims);

        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("test@test.com");
        when(claims.get("userRole", String.class)).thenReturn("USER");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(403, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("JWT 만료 시 인증 실패")
    void doFilter에_jwt만료로_실패한다() throws Exception {

        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        ExpiredJwtException expiredJwtException =
                new ExpiredJwtException(null, claims, "expired");

        when(jwtUtil.substringToken(any())).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenThrow(expiredJwtException);

        when(claims.getSubject()).thenReturn("1");

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("잘못된 JWT 형식이면 실패한다")
    void doFilter에_jwt검증실패로_실패한다() throws Exception {

        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.substringToken(any())).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenThrow(new MalformedJwtException("invalid"));

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(400, response.getStatus());
    }

    @Test
    @DisplayName("예상치 못한 오류가 발생한다")
    void doFilter에_예상치못한오류가_발생한다() throws Exception {

        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.substringToken(any())).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenThrow(new RuntimeException());

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(500, response.getStatus());
    }

    @Test
    @DisplayName("Claims 추출 실패로 인증이 필요합니다")
    void doFilter에_claims추출실패로_실패한다() throws Exception {

        request.setRequestURI("/todos");
        request.addHeader("Authorization", "Bearer token");

        when(jwtUtil.substringToken(any())).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(null);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("admin 경로에서 ADMIN 권한이면 통과")
    void doFilter에_admin권한이면_통과한다() throws Exception {

        request.setRequestURI("/admin/test");
        request.addHeader("Authorization", "Bearer token");

        Claims claims = mock(Claims.class);

        when(jwtUtil.substringToken(any())).thenReturn("token");
        when(jwtUtil.extractClaims("token")).thenReturn(claims);

        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("test@test.com");
        when(claims.get("userRole", String.class)).thenReturn("ADMIN");

        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(any(), any());
    }
}