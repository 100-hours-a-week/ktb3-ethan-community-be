package org.restapi.springrestapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.controller.support.ControllerTestSupport;
import org.restapi.springrestapi.dto.comment.CommentListResult;
import org.restapi.springrestapi.dto.comment.CommentResult;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.security.config.SecurityConfig;
import org.restapi.springrestapi.service.CommentService;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest extends ControllerTestSupport {

    @MockitoBean CommentService commentService;


    CustomUserDetails principal;
    final long POST_ID = 1L, COMMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        principal = new CustomUserDetails(UserFixture.persistedUser().toBuilder().id(1L).build());
    }

    @Test
    @DisplayName("댓글 생성 요청은 201과 데이터 본문을 반환한다")
    void createComment_returnsCreated() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest("hello");
        CommentResult result = CommentResult.builder()
                .userId(principal.getId())
                .content("hello")
                .build();
        given(commentService.createComment(principal.getId(), request, POST_ID))
                .willReturn(result);

        // when
        mockMvc.perform(post("/posts/{postId}/comments", POST_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GSON.toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value(request.content()));

        // then
        verify(commentService).createComment(principal.getId(), request, POST_ID);
    }

    @Test
    @DisplayName("댓글 생성 시 내용이 비어 있으면 400을 반환한다")
    void createComment_invalidRequest_returnsBadRequest() throws Exception {
        // given
        CreateCommentRequest invalid = new CreateCommentRequest("");

        // when
        mockMvc.perform(post("/posts/{postId}/comments", POST_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GSON.toJson(invalid)))
                .andExpect(status().isBadRequest());

        // then
        verifyNoInteractions(commentService);
    }

    @Test
    @DisplayName("커서/limit 생략 시 기본 limit=10으로 서비스가 호출되고 빈 결과를 그대로 반환한다")
    void getComments_withoutCursorUsesDefaultLimit() throws Exception {
        // given
        CommentListResult emptyResult = CommentListResult.empty();
        given(commentService.getCommentList(POST_ID, null, 10)).willReturn(emptyResult);

        // when
        mockMvc.perform(get("/posts/{postId}/comments", POST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.comments").doesNotExist())
                .andExpect(jsonPath("$.data.nextCursor").value(0));

        // then
        verify(commentService).getCommentList(POST_ID, null, 10);
    }

    @Test
    @DisplayName("댓글 수정 요청은 200을 반환한다")
    void patchComment_updates() throws Exception {
        PatchCommentRequest request = new PatchCommentRequest("updated");
        CommentResult result = CommentResult.builder()
                .content("updated")
                .build();
        given(commentService.updateComment(principal.getId(), request, POST_ID, COMMENT_ID))
                .willReturn(result);

        // when
        mockMvc.perform(patch("/posts/{postId}/comments/{id}", POST_ID, COMMENT_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GSON.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("updated"));

        // then
        verify(commentService).updateComment(principal.getId(), request, POST_ID, COMMENT_ID);
    }

    @Test
    @DisplayName("댓글 삭제 요청은 204를 반환한다")
    void deleteComment_deletes() throws Exception {
        // when
        mockMvc.perform(delete("/posts/{postId}/comments/{id}", POST_ID, COMMENT_ID)
                        .with(SecurityMockMvcRequestPostProcessors.user(principal)))
                .andExpect(status().isNoContent());

        // then
        verify(commentService).deleteComment(principal.getId(), POST_ID, COMMENT_ID);
    }
}
