package org.restapi.springrestapi.finder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.CommentErrorCode;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.repository.CommentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class CommentFinderTest {

    @InjectMocks
    CommentFinder commentFinder;

    @Mock
    CommentRepository commentRepository;

    @Test
    @DisplayName("findCommentSlice enforces minimum size when cursor missing")
    void findCommentSlice_withoutCursorEnforcesMinimumPageSize() {
        Slice<Comment> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 10), false);
        given(commentRepository.findSlice(eq(1L), any(PageRequest.class))).willReturn(slice);

        Slice<Comment> result = commentFinder.findCommentSlice(1L, null, 3);

        assertThat(result).isSameAs(slice);
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(commentRepository).findSlice(eq(1L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("findCommentSlice keeps large size when cursor exists")
    void findCommentSlice_withCursorKeepsLargePageSize() {
        Slice<Comment> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 15), false);
        given(commentRepository.findSlice(eq(1L), eq(50L), any(PageRequest.class))).willReturn(slice);

        Slice<Comment> result = commentFinder.findCommentSlice(1L, 50L, 15);

        assertThat(result).isSameAs(slice);
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(commentRepository).findSlice(eq(1L), eq(50L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(15);
    }

    @Test
    @DisplayName("findByIdOrThrow emits COMMENT_NOT_FOUND when missing")
    void findByIdOrThrow_throwsWhenCommentMissing() {
        given(commentRepository.findById(77L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentFinder.findByIdOrThrow(77L))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("findByIdOrThrow returns repository entity when present")
    void findByIdOrThrow_returnsCommentWhenPresent() {
        Comment comment = Comment.builder().id(12L).content("content").build();
        given(commentRepository.findById(12L)).willReturn(Optional.of(comment));

        Comment result = commentFinder.findByIdOrThrow(12L);

        assertThat(result).isSameAs(comment);
        verify(commentRepository).findById(12L);
    }
}
