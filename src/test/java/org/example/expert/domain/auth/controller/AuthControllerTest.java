package org.example.expert.domain.auth.controller;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void signup() {
        SignupRequest request =
                new SignupRequest("test@test.com", "pw", "USER");

        SignupResponse response =
                new SignupResponse("token");

        when(authService.signup(any())).thenReturn(response);

        SignupResponse result = authController.signup(request);

        assertEquals("token", result.getBearerToken());
    }

    @Test
    void signin() {
        SigninRequest request =
                new SigninRequest("test@test.com", "pw");

        SigninResponse response =
                new SigninResponse("token");

        when(authService.signin(any())).thenReturn(response);

        SigninResponse result = authController.signin(request);

        assertEquals("token", result.getBearerToken());
    }
}