package org.example.expert.domain.user.controller;

import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserAdminControllerTest {

    @Mock
    private UserAdminService userAdminService;

    @InjectMocks
    private UserAdminController userAdminController;

    @Test
    @DisplayName("유저 권한 변경에 성공한다")
    void changeUserRole에_성공한다() {
        // given
        long userId = 1L;
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        // when
        userAdminController.changeUserRole(userId, request);

        // then
        verify(userAdminService).changeUserRole(userId, request);
    }
}