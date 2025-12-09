package org.restapi.springrestapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.controller.support.ControllerTestSupport;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.security.config.SecurityConfig;
import org.restapi.springrestapi.service.post.PostLikeService;
import org.restapi.springrestapi.service.post.PostService;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
class PostControllerTest extends ControllerTestSupport {

    @MockitoBean PostService postService;
    @MockitoBean PostLikeService postLikeService;

    CustomUserDetails principal;

    @BeforeEach
    void setUp() {
        principal = new CustomUserDetails(UserFixture.persistedUser(1L));
    }

    @Test
    @DisplayName("게시글 등록 시 201 상태와 생성된 글 정보를 반환한다")
    void createPost_returnsCreated() throws Exception {
        CreatePostRequest request = new CreatePostRequest("제목", "내용", "thumb");
        PostResult response = samplePostResult(10L, "제목");
        given(postService.createPost(principal.getId(), request)).willReturn(response);

        mockMvc.perform(post("/posts")
                .with(SecurityMockMvcRequestPostProcessors.user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(GSON.toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").value(10));

        verify(postService).createPost(principal.getId(), request);
    }

    @Test
    @DisplayName("게시글 목록 조회 시 서비스 결과를 그대로 감싼다")
    void getPostList_returnsServicePayload() throws Exception {
        List<PostResult> posts = List.of(
            samplePostResult(5L, "첫"),
            samplePostResult(4L, "둘")
        );
        PostListResult listResult = PostListResult.from(posts, 3);
        given(postService.getPostList(20L, 2)).willReturn(listResult);

        mockMvc.perform(get("/posts")
                .param("cursor", "20")
                .param("limit", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.posts[0].id").value(5))
            .andExpect(jsonPath("$.data.nextCursor").value(3));

        verify(postService).getPostList(20L, 2);
    }

    @Test
    @DisplayName("게시글 상세 조회 시 인증 정보가 있다면 사용자 ID를 전달한다")
    void getPostDetail_passesPrincipalId() throws Exception {
        PostResult result = samplePostResult(7L, "상세");
        given(postService.getPost(any(), eq(principal.getId()), eq(7L)))
            .willReturn(result);

        mockMvc.perform(get("/posts/{id}", 7L)
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(7));

        verify(postService).getPost(any(), eq(principal.getId()), eq(7L));
    }

    @Test
    @DisplayName("게시글 상세 조회에 인증 정보가 없으면 null 사용자 ID로 호출한다")
    void getPostDetail_withoutPrincipal_passesNull() throws Exception {
        PostResult result = samplePostResult(5L, "상세-비로그인");
        given(postService.getPost(any(), isNull(), eq(5L)))
            .willReturn(result);

        mockMvc.perform(get("/posts/{id}", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(5));

        verify(postService).getPost(any(), isNull(), eq(5L));
   }

    @Test
    @DisplayName("게시글 수정은 인증된 사용자 ID로 위임된다")
    void patchPost_updatesPost() throws Exception {
        PatchPostRequest request = new PatchPostRequest("새제목", "새내용", "thumb", false);

        mockMvc.perform(patch("/posts/{id}", 3L)
                .with(SecurityMockMvcRequestPostProcessors.user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(GSON.toJson(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(SuccessCode.PATCH_SUCCESS.getCode()));

        verify(postService).updatePost(principal.getId(), 3L, request);
    }

    @Test
    @DisplayName("게시글 좋아요 토글을 요청하면 서비스에 위임한다")
    void togglePostLike() throws Exception {
        mockMvc.perform(patch("/posts/{id}/like", 6L)
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
            .andExpect(status().isOk());

        verify(postLikeService).togglePostLike(principal.getId(), 6L);
    }

    @Test
    @DisplayName("게시글 삭제는 204 상태를 반환한다")
    void deletePost_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/posts/{id}", 8L)
                .with(SecurityMockMvcRequestPostProcessors.user(principal)))
            .andExpect(status().isNoContent());

        verify(postService).deletePost(principal.getId(), 8L);
    }

    private PostResult samplePostResult(Long id, String title) {
        return PostResult.builder()
            .id(id)
            .userId(principal.getId())
            .userNickname("tester")
            .userProfileImageUrl("https://img")
            .title(title)
            .content("본문")
            .thumbnailImageUrl("thumb")
            .didLike(false)
            .likeCount(0)
            .commentCount(0)
            .viewCount(0)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
