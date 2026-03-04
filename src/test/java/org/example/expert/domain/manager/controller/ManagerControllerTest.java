package org.example.expert.domain.manager.controller;

import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private ManagerService managerService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JwtUtil jwtUtil;

    @InjectMocks
    private ManagerController managerController;

    @Test
    @DisplayName("매니저를 정상적으로 등록")
    void manager를_정상적으로_등록한다() {

        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        long todoId = 10L;
        ManagerSaveRequest request = new ManagerSaveRequest(2L);

        ManagerSaveResponse response =
                new ManagerSaveResponse(
                        100L,
                        new UserResponse(2L, "manager@test.com")
                );

        when(managerService.saveManager(authUser, todoId, request))
                .thenReturn(response);

        ResponseEntity<ManagerSaveResponse> result =
                managerController.saveManager(authUser, todoId, request);

        assertEquals(200, result.getStatusCodeValue());
        assertNotNull(result.getBody());
        assertEquals(100L, result.getBody().getId());
    }

    @Test
    @DisplayName("매니저 목록을 정상적으로 조회")
    void manager_목록을_정상적으로_조회한다() {

        long todoId = 10L;

        ManagerResponse response =
                new ManagerResponse(
                        1L,
                        new UserResponse(2L, "manager@test.com")
                );

        when(managerService.getManagers(todoId))
                .thenReturn(List.of(response));

        ResponseEntity<List<ManagerResponse>> result =
                managerController.getMembers(todoId);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(1, result.getBody().size());
    }

    @Test
    @DisplayName("매니저를 정상적으로 삭제")
    void manager를_정상적으로_삭제한다() {

        String bearerToken = "Bearer 123token";

        when(jwtUtil.extractClaims("123token").getSubject())
                .thenReturn("1");

        managerController.deleteManager(bearerToken, 10L, 20L);

        verify(managerService).deleteManager(1L, 10L, 20L);
    }
}