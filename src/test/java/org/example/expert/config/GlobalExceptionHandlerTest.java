package org.example.expert.config;

import static org.junit.jupiter.api.Assertions.*;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("invalidRequestExceptionException 예외 처리에 성공한다")
    void invalidRequestExceptionException에_성공한다() {

        InvalidRequestException ex = new InvalidRequestException("잘못된 요청");

        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.invalidRequestExceptionException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().get("status"));
        assertEquals(400, response.getBody().get("code"));
        assertEquals("잘못된 요청", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleAuthException 예외 처리에 성공한다")
    void handleAuthException에_성공한다() {

        AuthException ex = new AuthException("인증 실패");

        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.handleAuthException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().get("status"));
        assertEquals(401, response.getBody().get("code"));
        assertEquals("인증 실패", response.getBody().get("message"));
    }

    @Test
    @DisplayName("handleServerException 예외 처리에 성공한다")
    void handleServerException에_성공한다() {

        ServerException ex = new ServerException("서버 오류");

        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.handleServerException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().get("status"));
        assertEquals(500, response.getBody().get("code"));
        assertEquals("서버 오류", response.getBody().get("message"));
    }

    @Test
    @DisplayName("getErrorResponse 정상적으로 응답을 생성한다")
    void getErrorResponse에_성공한다() {

        ResponseEntity<Map<String, Object>> response =
                globalExceptionHandler.getErrorResponse(HttpStatus.BAD_REQUEST, "에러");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().get("status"));
        assertEquals(400, response.getBody().get("code"));
        assertEquals("에러", response.getBody().get("message"));
    }
}