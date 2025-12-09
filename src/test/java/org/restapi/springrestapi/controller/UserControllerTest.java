package org.restapi.springrestapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.dto.user.UserProfileResult;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.security.config.SecurityConfig;
import org.restapi.springrestapi.security.jwt.JwtFilter;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.service.UserService;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserService userService;
    @MockitoBean JwtProvider jwtProvider;
    @MockitoBean JwtFilter jwtFilter;

    CustomUserDetails principal;

    @BeforeEach
    void setUp() throws Exception {
        principal = new CustomUserDetails(UserFixture.persistedUser(1L));
        given(jwtProvider.resolveAccessToken(any(HttpServletRequest.class))).willReturn(Optional.empty());
        given(jwtProvider.resolveRefreshToken(any(HttpServletRequest.class))).willReturn(Optional.empty());

        doAnswer(invocation -> {
            HttpServletRequest req = invocation.getArgument(0);
            HttpServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);

            chain.doFilter(req, res);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("사용자 프로필 조회 응답을 반환한다")
    void getUserProfile_returnsResult() throws Exception {
        UserProfileResult result = UserProfileResult.builder()
            .id(5L)
            .email("user@test.com")
            .nickname("tester")
            .profileImageUrl("https://img")
            .build();
        given(userService.getUserProfile(5L)).willReturn(result);

        mockMvc.perform(get("/users/{id}", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nickname").value("tester"));

        verify(userService).getUserProfile(5L);
    }

    @Test
    @DisplayName("사용자 프로필 수정 요청을 위임한다")
    void updateProfile_delegatesToService() throws Exception {
        PatchProfileRequest request = new PatchProfileRequest("새닉", "https://img", false);

        mockMvc.perform(patch("/users")
                .with(SecurityMockMvcRequestPostProcessors.user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        verify(userService).updateProfile(principal.getId(), request);
    }

    @Test
    @DisplayName("비밀번호 변경은 204를 반환한다")
    void changePassword_updatesPassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("Password1!", "Password1!");

        mockMvc.perform(put("/users/password")
                .with(SecurityMockMvcRequestPostProcessors.user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(userService).updatePassword(eq(principal.user()), eq(request));
    }

    @Test
    @DisplayName("회원 탈퇴 시 서비스에 위임하고 204를 반환한다")
    void deleteUser_delegatesToService() throws Exception {
        mockMvc.perform(delete("/users")
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
            .andExpect(status().isNoContent());

        verify(userService).deleteUser(principal.getId());
    }

}
