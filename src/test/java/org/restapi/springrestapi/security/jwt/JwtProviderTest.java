package org.restapi.springrestapi.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.security.CustomUserDetails;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtProviderTest {

    @Mock UserFinder userFinder;

    JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        JwtProperties properties = new JwtProperties(
            new JwtProperties.Token("access-secret-access-secret-1234567890", Duration.ofMinutes(10)),
            new JwtProperties.Token("refresh-secret-refresh-secret-123456789012", Duration.ofDays(1))
        );
        jwtProvider = new JwtProvider(userFinder, properties);
        jwtProvider.init();
    }

    @Test
    @DisplayName("Access 토큰 생성 후 Subject를 다시 추출할 수 있다")
    void createAccessToken_containsUserId() {
        String token = jwtProvider.createAccessToken(5L);

        assertThat(jwtProvider.getUserIdFromAccess(token)).isEqualTo(5L);
        assertThat(jwtProvider.validateAccessToken(token)).isTrue();
    }

    @Test
    @DisplayName("Refresh 토큰은 Access 검증에 통과하지 않는다")
    void validateAccessToken_rejectsRefreshToken() {
        String refreshToken = jwtProvider.createRefreshToken(3L);

        assertThat(jwtProvider.validateRefreshToken(refreshToken)).isTrue();
        assertThat(jwtProvider.validateAccessToken(refreshToken)).isFalse();
    }

    @Test
    @DisplayName("Authorization 헤더에서 Access 토큰을 추출한다")
    void resolveAccessToken_readsHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token-value");

        Optional<String> token = jwtProvider.resolveAccessToken(request);

        assertThat(token).contains("token-value");
    }

    @Test
    @DisplayName("Refresh 쿠키가 없으면 예외를 던진다")
    void resolveRefreshToken_throwsWhenMissingCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> jwtProvider.resolveRefreshToken(request))
            .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("유효한 Refresh 쿠키에서 토큰을 읽어온다")
    void resolveRefreshToken_readsCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie(JwtProvider.REFRESH_COOKIE, "refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        Optional<String> token = jwtProvider.resolveRefreshToken(request);

        assertThat(token).contains("refresh-token");
    }

    @Test
    @DisplayName("getAuthentication은 UserFinder를 통해 Authentication을 구성한다")
    void getAuthentication_usesUserFinder() {
        String token = jwtProvider.createAccessToken(8L);
        User user = User.builder()
            .id(8L)
            .email("user@test.com")
            .nickname("tester")
            .password("encoded")
            .build();
        when(userFinder.findByIdOrAuthThrow(8L)).thenReturn(user);

        var authentication = jwtProvider.getAuthentication(token);

        verify(userFinder).findByIdOrAuthThrow(8L);
        assertThat(authentication.getPrincipal()).isInstanceOf(CustomUserDetails.class);
        assertThat(((CustomUserDetails) authentication.getPrincipal()).getId()).isEqualTo(8L);
    }
}
