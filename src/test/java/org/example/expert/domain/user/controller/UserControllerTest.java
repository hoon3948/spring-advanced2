package org.example.expert.domain.user.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("유저 조회에 성공한다")
    void getUser에_성공한다() {

        // given
        long userId = 1L;
        UserResponse response = new UserResponse(userId, "test@test.com");

        when(userService.getUser(userId)).thenReturn(response);

        // when
        ResponseEntity<UserResponse> result = userController.getUser(userId);

        // then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(userId, result.getBody().getId());
        assertEquals("test@test.com", result.getBody().getEmail());
    }

    @Test
    @DisplayName("비밀번호 변경에 성공한다")
    void changePassword에_성공한다() {

        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);

        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "newPassword");

        doNothing().when(userService).changePassword(authUser.getId(), request);

        // when
        userController.changePassword(authUser, request);

        // then
        verify(userService).changePassword(authUser.getId(), request);
    }
}