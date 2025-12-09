package org.restapi.springrestapi.integration;

import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.CommentRepository;
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
class CommentApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    private static final Gson GSON = new Gson();
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired JwtProvider jwtProvider;

    @Test
    @DisplayName("인증 사용자는 게시글에 댓글을 작성하고 DB에 저장된다")
    void createComment_persistsEntity() throws Exception {
        User user = userRepository.save(UserFixture.uniqueUser("commenter"));
        Post post = postRepository.save(Post.from(
            new CreatePostRequest("테스트 글", "본문", null),
            user
        ));
        CreateCommentRequest request = new CreateCommentRequest("첫 댓글");

        mockMvc.perform(post("/posts/{postId}/comments", post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearer(user.getId()))
                .content(GSON.toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.content").value(request.content()));

        assertThat(commentRepository.count()).isEqualTo(1);
        Comment saved = commentRepository.findAll().get(0);
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getPost().getId()).isEqualTo(post.getId());

        Post refreshed = postRepository.findById(post.getId()).orElseThrow();
        assertThat(refreshed.getCommentCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("인증 없이 댓글을 작성하면 401이 반환된다")
    void createComment_withoutAuth_returnsUnauthorized() throws Exception {
        User postAuthor = userRepository.save(UserFixture.uniqueUser("post"));
        Post post = postRepository.save(Post.from(
            new CreatePostRequest("테스트 글", "본문", null),
            postAuthor
        ));
        CreateCommentRequest request = new CreateCommentRequest("인증 필요");

        mockMvc.perform(post("/posts/{postId}/comments", post.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(GSON.toJson(request)))
            .andExpect(status().isUnauthorized());
    }

    private String bearer(Long userId) {
        return "Bearer " + jwtProvider.createAccessToken(userId);
    }
}
