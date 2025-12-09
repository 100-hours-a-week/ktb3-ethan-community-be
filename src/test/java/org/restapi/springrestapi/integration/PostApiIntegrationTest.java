package org.restapi.springrestapi.integration;

import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    private static final Gson GSON = new Gson();
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired JwtProvider jwtProvider;

    @Test
    @DisplayName("인증 사용자는 게시글을 생성하고 201을 응답받는다")
    void createPost_returnsCreatedWithPersistedEntity() throws Exception {
        User user = userRepository.save(UserFixture.uniqueUser("writer"));
        CreatePostRequest request = new CreatePostRequest("통합 제목", "통합 본문", "thumb");

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                .content(GSON.toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value(request.title()))
            .andExpect(jsonPath("$.data.content").value(request.content()));

        assertThat(postRepository.count()).isEqualTo(1);
        Post saved = postRepository.findAll().get(0);
        assertThat(saved.getAuthor().getId()).isEqualTo(user.getId());
        assertThat(saved.getTitle()).isEqualTo(request.title());
    }

    @Test
    @DisplayName("탈퇴한 사용자가 기존 토큰으로 게시글을 작성하면 401을 반환한다")
    void createPost_deletedUser_returnsUnauthorized() throws Exception {
        User user = userRepository.save(UserFixture.uniqueUser("deleted"));
        String token = bearer(user.getId());
        userRepository.delete(user);
        userRepository.flush();

        CreatePostRequest request = new CreatePostRequest("무효", "삭제된 사용자", null);

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .content(GSON.toJson(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(AuthErrorCode.UNAUTHORIZED.getCode()))
            .andExpect(jsonPath("$.message").value(AuthErrorCode.UNAUTHORIZED.getMessage()));
    }

    @Test
    @DisplayName("잘못된 게시글 요청 본문은 400을 반환한다")
    void createPost_invalidRequest_returnsBadRequest() throws Exception {
        User user = userRepository.save(UserFixture.uniqueUser("invalid"));
        CreatePostRequest request = new CreatePostRequest(" ", "본문", null);

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                .content(GSON.toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("게시글 제목은 공백일 수 없습니다."));
    }

    private String bearer(Long userId) {
        return "Bearer " + jwtProvider.createAccessToken(userId);
    }
}
