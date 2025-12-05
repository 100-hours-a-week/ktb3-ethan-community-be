package org.restapi.springrestapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.comment.CommentListResult;
import org.restapi.springrestapi.dto.comment.CommentResult;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.finder.CommentFinder;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.CommentRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.service.comment.CommentServiceImpl;
import org.restapi.springrestapi.validator.CommentValidator;
import org.restapi.springrestapi.validator.PostValidator;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock CommentFinder commentFinder;
    @Mock CommentValidator commentValidator;
    @Mock PostFinder postFinder;
    @Mock PostValidator postValidator;
    @Mock UserFinder userFinder;
    @Mock UserValidator userValidator;

    @InjectMocks CommentServiceImpl commentService;

    @Test
    @DisplayName("댓글 등록 시 사용자/게시글 검증 후 댓글을 저장하고 게시글 댓글 수를 증가시킨다")
    void registerComment_persistsCommentAndIncrementsCount() {
        // given
        Long userId = 10L;
        Long postId = 5L;
        CreateCommentRequest request = new CreateCommentRequest("댓글");
        User writer = sampleUser(userId);
        Post post = samplePost(postId);
        Comment saved = sampleComment(20L, writer, post);

        given(userFinder.findProxyById(userId)).willReturn(writer);
        given(postFinder.findProxyById(postId)).willReturn(post);
        given(commentRepository.save(any(Comment.class))).willReturn(saved);

        // when
        CommentResult result = commentService.createComment(userId, request, postId);

        // then
        verify(userValidator).validateUserExists(userId);
        verify(postValidator).validatePostExists(postId);
        verify(postRepository).increaseCommentCount(postId);
        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.content()).isEqualTo(saved.getContent());
    }

    @Test
    @DisplayName("댓글 목록을 조회하면 CommentResult 리스트와 다음 커서를 계산한다")
    void getCommentList_returnsResultsAndNextCursor() {
        // given
        Long postId = 3L;
        Comment first = sampleComment(6L, sampleUser(1L), samplePost(postId));
        Comment second = sampleComment(2L, sampleUser(2L), samplePost(postId));
        Slice<Comment> slice = new SliceImpl<>(List.of(first, second), PageRequest.of(0, 2), false);
        given(commentFinder.findCommentSlice(postId, null, 2)).willReturn(slice);

        // when
        CommentListResult result = commentService.getCommentList(postId, null, 2);

        // then
        verify(postValidator).validatePostExists(postId);
        assertThat(result.comments()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(3);
    }

    @Test
    @DisplayName("댓글이 없으면 빈 결과를 반환한다")
    void getCommentList_emptyReturnsEmptyResult() {
        // given
        Long postId = 7L;
        Slice<Comment> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 10), false);
        given(commentFinder.findCommentSlice(postId, 10L, 10)).willReturn(slice);

        // when
        CommentListResult result = commentService.getCommentList(postId, 10L, 10);

        // then
        verify(postValidator).validatePostExists(postId);
        assertThat(result.comments()).isNull();
        assertThat(result.nextCursor()).isZero();
    }

    @Test
    @DisplayName("댓글 수정 시 소유자 검증 후 내용을 갱신한다")
    void updateComment_updatesContent() {
        // given
        Long userId = 4L;
        Long postId = 8L;
        Long commentId = 2L;
        PatchCommentRequest request = new PatchCommentRequest("updated");
        Comment comment = sampleComment(commentId, sampleUser(userId), samplePost(postId));
        Comment saved = comment.toBuilder().content(request.content()).build();
        given(commentFinder.findById(commentId)).willReturn(comment);
        given(commentRepository.save(comment)).willReturn(saved);

        // when
        CommentResult result = commentService.updateComment(userId, request, postId, commentId);

        // then
        verify(postValidator).validatePostExists(postId);
        verify(commentValidator).validateOwner(commentId, userId);
        assertThat(result.content()).isEqualTo(request.content());
    }

    @Test
    @DisplayName("댓글 삭제 시 검증 후 게시글 카운트를 감소시키고 댓글을 삭제한다")
    void deleteComment_deletesAndDecrementsCount() {
        // given
        Long userId = 4L;
        Long postId = 9L;
        Long commentId = 12L;
        Comment comment = spy(sampleComment(commentId, sampleUser(userId), samplePost(postId)));
        given(commentFinder.findById(commentId)).willReturn(comment);

        // when
        commentService.deleteComment(userId, postId, commentId);

        // then
        verify(userValidator).validateUserExists(userId);
        verify(postValidator).validatePostExists(postId);
        verify(commentValidator).validateCommentExists(commentId);
        verify(commentValidator).validateOwner(commentId, userId);
        verify(postRepository).decreaseCommentCount(postId);
        verify(commentRepository).deleteById(commentId);
        verify(comment).changePost(null);
    }

    private User sampleUser(Long id) {
        return User.builder()
                .id(id)
                .nickname("user" + id)
                .email("user" + id + "@test.com")
                .password("pw")
                .profileImageUrl("https://img/" + id)
                .joinAt(LocalDateTime.now())
                .build();
    }

    private Post samplePost(Long id) {
        return Post.builder()
                .id(id)
                .title("title " + id)
                .content("content")
                .thumbnailImageUrl("thumb")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .author(sampleUser(100L))
                .build();
    }

    private Comment sampleComment(Long id, User user, Post post) {
        Comment comment = Comment.builder()
                .id(id)
                .content("comment " + id)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .post(post)
                .build();
        post.getComments().add(comment);
        user.getComments().add(comment);
        return comment;
    }
}
