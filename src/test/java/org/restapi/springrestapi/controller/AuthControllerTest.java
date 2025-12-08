package org.restapi.springrestapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.restapi.springrestapi.dto.auth.LoginRequest;
import org.restapi.springrestapi.dto.auth.LoginResult;
import org.restapi.springrestapi.dto.auth.RefreshTokenResult;
import org.restapi.springrestapi.dto.auth.SignUpRequest;
import org.restapi.springrestapi.security.jwt.JwtFilter;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
	@MockitoBean JwtProvider jwtProvider;
	@MockitoBean JwtFilter jwtFilter;

    @Test
    @DisplayName("로그인 성공 시 토큰 응답과 쿠키를 설정한다")
    void login_returnsTokenAndCookie() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "Password1!");
        LoginResult result = LoginResult.builder()
            .id(1L)
            .email(request.email())
            .nickname("tester")
            .accessToken("access")
            .refreshCookie(ResponseCookie.from("refresh_token", "refresh").build())
            .build();
        given(authService.login(request)).willReturn(result);

        mockMvc.perform(post("/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, result.refreshCookie().toString()))
            .andExpect(jsonPath("$.data.accessToken").value("access"));

        verify(authService).login(request);
    }

    @Test
    @DisplayName("회원가입 시에도 LoginResponse 를 반환한다")
    void signup_returnsLoginResponse() throws Exception {
        SignUpRequest request = new SignUpRequest("new@test.com", "Password1!", "nick", null);
        LoginResult result = LoginResult.builder()
            .id(2L)
            .email(request.email())
            .nickname(request.nickname())
            .accessToken("access")
            .refreshCookie(ResponseCookie.from("refresh_token", "refresh").build())
            .build();
        given(authService.signup(request)).willReturn(result);

        mockMvc.perform(post("/auth/signup")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(request.email()));

        verify(authService).signup(request);
    }

    @Test
    @DisplayName("refresh 요청은 새 액세스 토큰을 반환한다")
    void refresh_returnsNewToken() throws Exception {
        RefreshTokenResult refreshResult = new RefreshTokenResult(
            "new-access",
            ResponseCookie.from("refresh_token", "new-refresh").build()
        );
        given(authService.refresh(any())).willReturn(refreshResult);

        mockMvc.perform(post("/auth/refresh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("new-access"));

        verify(authService).refresh(any());
    }

    @Test
    @DisplayName("로그아웃은 리프레시 쿠키를 삭제하고 204를 반환한다")
    void logout_clearsCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
            .andExpect(status().isNoContent());
    }
}
