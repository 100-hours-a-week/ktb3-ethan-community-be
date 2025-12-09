package org.restapi.springrestapi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.auth.LoginRequest;
import org.restapi.springrestapi.dto.auth.LoginResult;
import org.restapi.springrestapi.dto.auth.RefreshTokenResult;
import org.restapi.springrestapi.dto.auth.SignUpRequest;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtProvider jwtProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserValidator userValidator;
    @Mock UserRepository userRepository;
    @Mock UserFinder userFinder;

    @InjectMocks AuthService authService;

    @Nested
    class Login {
        @Test
        @DisplayName("올바른 자격 증명으로 로그인하면 토큰과 쿠키를 반환한다")
        void login_success_returnsTokens() {
            LoginRequest request = new LoginRequest("user@test.com", "Password1!");
            User user = UserFixture.persistedUser().toBuilder()
                .id(99L)
                .email(request.email())
                .build();
            CustomUserDetails principal = new CustomUserDetails(user);

            Authentication authentication = mock(Authentication.class);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
            given(authentication.getPrincipal()).willReturn(principal);
            given(jwtProvider.createAccessToken(user.getId())).willReturn("access-token");
            given(jwtProvider.createRefreshToken(user.getId())).willReturn("refresh-token");
            ResponseCookie cookie = ResponseCookie.from("refresh_token", "refresh-token").build();
            given(jwtProvider.createRefreshCookie("refresh-token")).willReturn(cookie);

            LoginResult result = authService.login(request);

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            assertThat(result.id()).isEqualTo(user.getId());
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshCookie()).isEqualTo(cookie);
        }

        @Test
        @DisplayName("인증 실패 시 INVALID_EMAIL_OR_PASSWORD 예외를 던진다")
        void login_failureThrowsAppException() {
            LoginRequest request = new LoginRequest("user@test.com", "wrong");
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("bad"));

            assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .hasMessage(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD.getMessage());
        }
    }

    @Nested
    class Signup {
        @Test
        @DisplayName("회원 가입 시 중복 검증, 패스워드 인코딩, 토큰 발급이 이루어진다")
        void signup_success_persistsUserAndReturnsTokens() {
            SignUpRequest request = new SignUpRequest(
                "new@user.com",
                "Password1!",
                "nickname",
                "http://img"
            );

            User saved = UserFixture.persistedUser().toBuilder()
                .id(1L)
                .email(request.email())
                .nickname(request.nickname())
                .build();

            given(passwordEncoder.encode(request.password())).willReturn("encoded");
            given(userRepository.save(any(User.class))).willReturn(saved);
            given(jwtProvider.createAccessToken(saved.getId())).willReturn("access");
            given(jwtProvider.createRefreshToken(saved.getId())).willReturn("refresh");
            ResponseCookie cookie = ResponseCookie.from("refresh_token", "refresh").build();
            given(jwtProvider.createRefreshCookie("refresh")).willReturn(cookie);

            LoginResult result = authService.signup(request);

            verify(userValidator).validateSignUpUser(request.email(), request.nickname());
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
            assertThat(result.accessToken()).isEqualTo("access");
            assertThat(result.refreshCookie()).isEqualTo(cookie);
        }
    }

    @Nested
    class RefreshToken {
        @Test
        @DisplayName("refresh 요청 시 토큰 값을 재발급하고 회원 존재 여부를 확인한다")
        void refresh_success_reissuesTokens() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            given(jwtProvider.resolveRefreshToken(request)).willReturn(Optional.of("old-refresh"));
            given(jwtProvider.getUserIdFromRefresh("old-refresh")).willReturn(7L);
            given(jwtProvider.createAccessToken(7L)).willReturn("new-access");
            given(jwtProvider.createRefreshToken(7L)).willReturn("new-refresh");
            ResponseCookie cookie = ResponseCookie.from("refresh_token", "new-refresh").build();
            given(jwtProvider.createRefreshCookie("new-refresh")).willReturn(cookie);

            RefreshTokenResult result = authService.refresh(request);

            verify(userFinder).existsByIdOrThrow(7L);
            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshCookie()).isEqualTo(cookie);
        }

        @Test
        @DisplayName("refresh 토큰이 없으면 REFRESH_COOKIE_MISSING 예외를 던진다")
        void refresh_missingToken_throws() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            given(jwtProvider.resolveRefreshToken(request)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(AppException.class)
                .hasMessage(AuthErrorCode.REFRESH_COOKIE_MISSING.getMessage());
        }
    }
}
