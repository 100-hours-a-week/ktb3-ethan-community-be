package org.restapi.springrestapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.comment.CommentListResult;
import org.restapi.springrestapi.dto.comment.CommentResult;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.CommentErrorCode;
import org.restapi.springrestapi.finder.CommentFinder;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.CommentRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.support.fixture.CommentFixture;
import org.restapi.springrestapi.support.fixture.PostFixture;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    CommentService commentService;

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock CommentFinder commentFinder;
    @Mock PostFinder postFinder;
    @Mock UserFinder userFinder;

    @Test
    @DisplayName("댓글 작성 시 게시글 존재 여부를 확인하고 저장 및 집계를 수행한다")
    void createComment_persistsEntityAndIncrementsCount() {
		// given
        Long userId = 1L;
        Long postId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("첫 댓글");
        User author = UserFixture.persistedUser(userId);
        Post post = PostFixture.persistedPost(postId, author);
        Comment savedComment = CommentFixture.persistedComment(1L, author, post);

        given(userFinder.findProxyById(userId)).willReturn(author);
        given(postFinder.findProxyById(postId)).willReturn(post);
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

		// when
        CommentResult result = commentService.createComment(userId, request, postId);

		// then
		verify(postFinder).existsByIdOrThrow(postId);
        verify(postRepository).increaseCommentCount(postId);
        verify(commentRepository).save(any(Comment.class));
        assertThat(result.id()).isEqualTo(savedComment.getId());
        assertThat(result.content()).isEqualTo(savedComment.getContent());
    }

    @Test
    @DisplayName("댓글 목록 조회 시 DTO 리스트와 다음 커서를 반환한다")
    void getCommentList_returnsDtoListAndNextCursor() {
		// given
        Long postId = 1L;
		Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(1L));
        Comment first = CommentFixture.persistedComment(1L, UserFixture.persistedUser(2L), post);
        Comment second = CommentFixture.persistedComment(2L, UserFixture.persistedUser(3L), post);
        Slice<Comment> slice = new SliceImpl<>(List.of(first, second), PageRequest.of(0, 2), false);
        given(commentFinder.findCommentSlice(postId, null, 2)).willReturn(slice);

		// when
        CommentListResult result = commentService.getCommentList(postId, null, 2);

		// then
        verify(postFinder).existsByIdOrThrow(postId);
        assertThat(result.comments()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(3); // last id 2 -> nextCursor 3
    }

    @Test
    @DisplayName("댓글이 없으면 빈 결과를 반환한다")
    void getCommentList_returnsEmptyResultWhenNoComment() {
        Long postId = 2L;
        Slice<Comment> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 3), false);
        given(commentFinder.findCommentSlice(postId, 1L, 3)).willReturn(emptySlice);

        CommentListResult result = commentService.getCommentList(postId, 1L, 3);

        verify(postFinder).existsByIdOrThrow(postId);
        assertThat(result.comments()).isNull();
        assertThat(result.nextCursor()).isZero();
    }

    @Test
    @DisplayName("작성자가 맞고 게시글이 일치하면 댓글 내용을 수정한다")
    void updateComment_updatesContentWhenPermissionMatches() {
        Long userId = 5L;
        Long postId = 7L;
        Long commentId = 3L;
        PatchCommentRequest request = new PatchCommentRequest("수정된 내용");
        Comment comment = CommentFixture.persistedComment(
            commentId,
            UserFixture.persistedUser(userId),
            PostFixture.persistedPost(postId, UserFixture.persistedUser(9L))
        );
        given(commentFinder.findByIdOrThrow(commentId)).willReturn(comment);

        CommentResult result = commentService.updateComment(userId, request, postId, commentId);

        assertThat(result.content()).isEqualTo(request.content());
        assertThat(comment.getContent()).isEqualTo(request.content());
    }

    @Test
    @DisplayName("작성자가 아니면 댓글 수정 시 예외가 발생한다")
    void updateComment_throwsWhenNotAuthor() {
        Long commentId = 3L;
        Comment comment = CommentFixture.persistedComment(
            commentId,
            UserFixture.persistedUser(20L),
            PostFixture.persistedPost(2L, UserFixture.persistedUser(30L))
        );
        given(commentFinder.findByIdOrThrow(commentId)).willReturn(comment);

        assertThatThrownBy(() -> commentService.updateComment(1L, new PatchCommentRequest("수정"), 2L, commentId))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(CommentErrorCode.NOT_COMMENT_OWNER));
    }

    @Test
    @DisplayName("작성자가 맞으면 댓글 삭제 및 카운트를 감소시킨다")
    void deleteComment_deletesWhenPermissionMatches() {
        Long userId = 5L;
        Long postId = 8L;
        Long commentId = 12L;
        Comment comment = CommentFixture.persistedComment(
            commentId,
            UserFixture.persistedUser(userId),
            PostFixture.persistedPost(postId, UserFixture.persistedUser(9L))
        );
        given(commentFinder.findByIdOrThrow(commentId)).willReturn(comment);

        commentService.deleteComment(userId, postId, commentId);

        verify(postRepository).decreaseCommentCount(postId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("작성자가 아니면 댓글 삭제 시 예외가 발생한다")
    void deleteComment_throwsWhenNotAuthor() {
        Long commentId = 12L;
        Comment comment = CommentFixture.persistedComment(
            commentId,
            UserFixture.persistedUser(20L),
            PostFixture.persistedPost(8L, UserFixture.persistedUser(30L))
        );
        given(commentFinder.findByIdOrThrow(commentId)).willReturn(comment);

        assertThatThrownBy(() -> commentService.deleteComment(1L, 8L, commentId))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(CommentErrorCode.NOT_COMMENT_OWNER));
        verify(commentRepository, never()).deleteById(anyLong());
    }

}
