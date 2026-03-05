package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 조회 성공")
    void user_조회에_성공한다() {

        Long userId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);

        ReflectionTestUtils.setField(user, "id", userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(userId);

        assertEquals(userId, response.getId());
        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    @DisplayName("유저 조회시 유저가 없으면 예외 발생")
    void user_조회_시_user가_없으면_예외_발생() {

        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        InvalidRequestException exception =
                assertThrows(InvalidRequestException.class,
                        () -> userService.getUser(userId));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경에 성공한다")
    void changePassword에_성공한다() {

        Long userId = 1L;

        User user = new User("test@test.com", "encodedOldPassword", UserRole.USER);

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword", "newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("newPassword", "encodedOldPassword"))
                .thenReturn(false);

        when(passwordEncoder.matches("oldPassword", "encodedOldPassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        userService.changePassword(userId, request);

        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    @DisplayName("새 비밀번호가 기존비밀번호와 같으면 예외 발생")
    void newPassword가_oldPassword과_같으면_예외_발생() {

        Long userId = 1L;

        User user = new User("test@test.com", "encodedPassword", UserRole.USER);

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("oldPassword", "samePassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("samePassword", "encodedPassword"))
                .thenReturn(true);

        InvalidRequestException exception =
                assertThrows(InvalidRequestException.class,
                        () -> userService.changePassword(userId, request));

        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("기존 비밀번호가 틀리면 예외 발생")
    void oldPassword가_틀리면_예외_발생() {

        Long userId = 1L;

        User user = new User("test@test.com", "encodedPassword", UserRole.USER);

        UserChangePasswordRequest request =
                new UserChangePasswordRequest("wrongOldPassword", "newPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches("newPassword", "encodedPassword"))
                .thenReturn(false);

        when(passwordEncoder.matches("wrongOldPassword", "encodedPassword"))
                .thenReturn(false);

        InvalidRequestException exception =
                assertThrows(InvalidRequestException.class,
                        () -> userService.changePassword(userId, request));

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }
}