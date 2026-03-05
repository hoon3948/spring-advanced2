package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    @DisplayName("유저 권한 변경에 성공한다")
    void changeUserRole에_성공한다() {

        // given
        long userId = 1L;

        User user = new User("test@test.com", "password", UserRole.USER);

        UserRoleChangeRequest request = new UserRoleChangeRequest("Admin");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(userId, request);

        // then
        assertEquals(UserRole.ADMIN, user.getUserRole());
    }

    @Test
    @DisplayName("유저 권한 변경 시 유저가 없으면 예외가 발생한다")
    void changeUserRole시_유저가_없으면_예외가_발생한다() {

        // given
        long userId = 1L;

        UserRoleChangeRequest request = new UserRoleChangeRequest("Admin");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        InvalidRequestException exception =
                assertThrows(InvalidRequestException.class,
                        () -> userAdminService.changeUserRole(userId, request));

        // then
        assertEquals("User not found", exception.getMessage());
    }
}