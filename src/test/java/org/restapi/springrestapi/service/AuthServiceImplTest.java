package org.restapi.springrestapi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.auth.LoginRequest;
import org.restapi.springrestapi.dto.auth.LoginResult;
import org.restapi.springrestapi.dto.auth.RefreshTokenResult;
import org.restapi.springrestapi.dto.auth.SignUpRequest;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.service.auth.AuthServiceImpl;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtProvider jwtProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock
    UserValidator userValidator;
    @Mock UserRepository userRepository;

    @InjectMocks AuthServiceImpl authService;

    @Test
    @DisplayName("로그인 성공 시 토큰과 쿠키를 생성해 반환한다")
    void login_success_returnsTokens() {
        // given
        LoginRequest request = new LoginRequest("user@test.com", "pw1234!");
        User user = sampleUser(1L);
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(new CustomUserDetails(user));
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtProvider.createAccessToken(user.getId())).willReturn("access");
        given(jwtProvider.createRefreshToken(user.getId())).willReturn("refresh");
        ResponseCookie refreshCookie = ResponseCookie.from("refresh", "value").build();
        given(jwtProvider.createRefreshCookie("refresh")).willReturn(refreshCookie);

        // when
        LoginResult result = authService.login(request);

        // then
        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshCookie()).isEqualTo(refreshCookie);
    }

    @Test
    @DisplayName("로그인 실패 시 AppException을 던진다")
    void login_failure_throwsAppException() {
        // given
        LoginRequest request = new LoginRequest("user@test.com", "wrong");
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("bad"));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("회원가입 시 중복 검사, 비밀번호 인코딩, 토큰 발급이 순차적으로 수행된다")
    void signup_createsUserAndTokens() {
        // given
        SignUpRequest request = new SignUpRequest("user@test.com", "pw1234!", "tester", "https://img");
        given(passwordEncoder.encode(request.password())).willReturn("encoded");
        User saved = sampleUser(10L);
        given(userRepository.save(any(User.class))).willReturn(saved);
        given(jwtProvider.createAccessToken(saved.getId())).willReturn("new-access");
        given(jwtProvider.createRefreshToken(saved.getId())).willReturn("new-refresh");
        ResponseCookie cookie = ResponseCookie.from("refresh", "value").build();
        given(jwtProvider.createRefreshCookie("new-refresh")).willReturn(cookie);

        // when
        LoginResult result = authService.signup(request);

        // then
        verify(userValidator).validateSignUpUser(request.email(), request.nickname());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshCookie()).isEqualTo(cookie);
    }

    @Test
    @DisplayName("리프레시 토큰 갱신 시 사용자 존재를 검증하고 새 토큰/쿠키를 발급한다")
    void refreshToken_success_returnsNewTokens() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(jwtProvider.resolveRefreshToken(request)).willReturn(Optional.of("refresh"));
        given(jwtProvider.getUserIdFromRefresh("refresh")).willReturn(5L);
        given(jwtProvider.createAccessToken(5L)).willReturn("access2");
        given(jwtProvider.createRefreshToken(5L)).willReturn("refresh2");
        ResponseCookie cookie = ResponseCookie.from("refresh", "another").build();
        given(jwtProvider.createRefreshCookie("refresh2")).willReturn(cookie);

        // when
        RefreshTokenResult result = authService.refresh(request);

        // then
        verify(userValidator).validateUserExists(5L);
        assertThat(result.accessToken()).isEqualTo("access2");
        assertThat(result.refreshCookie()).isEqualTo(cookie);
    }

    private User sampleUser(Long id) {
        return User.builder()
                .id(id)
                .nickname("tester")
                .email("tester@test.com")
                .password("encoded")
                .profileImageUrl("https://img")
                .joinAt(LocalDateTime.now())
                .build();
    }
}
