package org.restapi.springrestapi.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.security.jwt.JwtFilter;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired JwtProvider jwtProvider;
    @Autowired
    JwtFilter jwtFilter;
    @Test
    @DisplayName("유효한 리프레시 쿠키로 /auth/refresh 요청 시 새 토큰과 쿠키를 반환한다")
    void refresh_returnsNewTokens() throws Exception {
        User user = userRepository.save(UserFixture.uniqueUser("refresh"));
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        Cookie refreshCookie = new Cookie(JwtProvider.REFRESH_COOKIE, refreshToken);
        refreshCookie.setPath(JwtProvider.REFRESH_PATH);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);

        mockMvc.perform(post("/auth/refresh").cookie(refreshCookie).with(csrf()))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.access_token").isNotEmpty())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(JwtProvider.REFRESH_COOKIE + "=")));
    }

    @Test
    @DisplayName("리프레시 쿠키가 없으면 /auth/refresh는 400을 반환한다")
    void refresh_withoutCookie_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/refresh").with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(AuthErrorCode.COOKIE_MISSING.getCode()))
            .andExpect(jsonPath("$.message").value(AuthErrorCode.COOKIE_MISSING.getMessage()));
    }
}
