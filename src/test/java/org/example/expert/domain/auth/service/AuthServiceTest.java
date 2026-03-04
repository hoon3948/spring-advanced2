package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private SigninRequest signinRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest(
                "test@test.com",
                "password",
                "USER"
        );

        signinRequest = new SigninRequest(
                "test@test.com",
                "password"
        );
    }

    @Test
    @DisplayName("이메일 중복 시 예외발생")
    void signup_회원가입_중_이메일이_중복되어_에러_발생() {
        //given\
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        //when
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class, () -> {
                    authService.signup(signupRequest);
                }
        );

        //then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("회원가입 성공, 토큰 반환")
    void signup_회원가입에_성공(){
        given(userRepository.existsByEmail(anyString()))
                .willReturn(false);

        given(passwordEncoder.encode(anyString()))
                .willReturn("encodedPassword");

        User user = new User(
                signupRequest.getEmail(),
                "encodedPassword",
                UserRole.USER
        );

        given(userRepository.save(any(User.class)))
                .willReturn(user);

        ReflectionTestUtils.setField(user, "id", 1L);
        // 유저id가 null이라서 아래에서 터짐 --> 위 코드 추가
        given(jwtUtil.createToken(anyLong(), anyString(), any()))
                .willReturn("Bearer token");

        SignupResponse response = authService.signup(signupRequest);

        assertNotNull(response);
        assertEquals("Bearer token", response.getBearerToken());
    }

    @Test
    @DisplayName("존재하지 않는 이메일 예외처리")
    void signin_로그인_중_존재하지_않는_이메일_예외_발생(){
        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        //when
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class, () -> {
                    authService.signin(signinRequest);
                }
        );

        //then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호 불일치 예외처리")
    void signin_로그인_중_비밀번호_불일치_예외_발생(){
        //given
        User user = new User(
                signinRequest.getEmail(),
                "encodedPassword",
                UserRole.USER
        );
        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(false);

        //when
        AuthException exception = assertThrows(
                AuthException.class, () -> {
                    authService.signin(signinRequest);
                }
        );

        //then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("정상 로그인, 토큰 반환")
    void signin_로그인에_성공(){
        User user = new User(
                signinRequest.getEmail(),
                "encodedPassword",
                UserRole.USER
        );

        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.of(user));

        given(passwordEncoder.matches(anyString(), anyString()))
                .willReturn(true);

        ReflectionTestUtils.setField(user, "id", 1L);

        given(jwtUtil.createToken(anyLong(), anyString(), any()))
                .willReturn("Bearer token");

        SigninResponse response = authService.signin(signinRequest);

        assertNotNull(response);
        assertEquals("Bearer token", response.getBearerToken());
    }
}