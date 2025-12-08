package org.restapi.springrestapi.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock JwtProvider jwtProvider;
    @Mock AuthenticationEntryPoint authenticationEntryPoint;

    @InjectMocks JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("OPTIONS 프리플라이트 요청은 필터를 건너뛴다")
    void optionsRequest_skipsFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(request, response, new MockFilterChain());

        verify(jwtProvider, never()).resolveAccessToken(any());
    }

    @Test
    @DisplayName("Refresh 엔드포인트에서 토큰이 유효하지 않으면 EntryPoint가 호출된다")
    void refreshRequest_invalidTokenTriggersEntryPoint() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", JwtProvider.REFRESH_PATH);
        request.setCookies(new Cookie(JwtProvider.REFRESH_COOKIE, "bad"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtProvider.resolveRefreshToken(request)).thenReturn(Optional.of("bad"));
        when(jwtProvider.validateRefreshToken("bad")).thenReturn(false);

        jwtFilter.doFilter(request, response, new MockFilterChain());

        verify(authenticationEntryPoint).commence(any(), any(), any());
    }

    @Test
    @DisplayName("유효한 Access 토큰이면 SecurityContext 에 Authentication 이 설정된다")
    void validAccessToken_setsAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/posts");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = mock(Authentication.class);

        when(jwtProvider.resolveAccessToken(request)).thenReturn(Optional.of("token"));
        when(jwtProvider.validateAccessToken("token")).thenReturn(true);
        when(jwtProvider.getAuthentication("token")).thenReturn(authentication);

        jwtFilter.doFilter(request, response, new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        verify(authenticationEntryPoint, never()).commence(any(), any(), any());
    }
}
