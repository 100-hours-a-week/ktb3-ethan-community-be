package org.restapi.springrestapi.finder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.PostErrorCode;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.repository.PostLikeRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class PostFinderTest {

    @InjectMocks
    PostFinder postFinder;

    @Mock
    PostRepository postRepository;
    @Mock
    PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("findPostSummarySlice clamps lower bound without cursor")
    void findPostSummarySlice_withoutCursorClampsLowerBound() {
        Slice<PostResult> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 1), false);
        given(postRepository.findSlice(any(PageRequest.class))).willReturn(slice);

        Slice<PostResult> result = postFinder.findPostSummarySlice(null, 0);

        assertThat(result).isSameAs(slice);
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(postRepository).findSlice(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("findPostSummarySlice clamps upper bound when cursor exists")
    void findPostSummarySlice_withCursorClampsUpperBound() {
        Slice<PostResult> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 10), false);
        given(postRepository.findSlice(eq(100L), any(PageRequest.class))).willReturn(slice);

        Slice<PostResult> result = postFinder.findPostSummarySlice(100L, 30);

        assertThat(result).isSameAs(slice);
        ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(postRepository).findSlice(eq(100L), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("isDidLikeUser short-circuits when userId is null")
    void isDidLikeUser_returnsFalseWhenUserIsNull() {
        boolean result = postFinder.isDidLikeUser(5L, null);

        assertThat(result).isFalse();
        verify(postLikeRepository, never()).existsByUserIdAndPostId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("findByIdOrThrow emits POST_NOT_FOUND when missing")
    void findByIdOrThrow_throwsWhenPostMissing() {
        given(postRepository.findById(7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postFinder.findByIdOrThrow(7L))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(PostErrorCode.POST_NOT_FOUND));
    }

    @Test
    @DisplayName("isDidLikeUser mirrors repository response")
    void isDidLikeUser_returnsRepositoryResult() {
        given(postLikeRepository.existsByUserIdAndPostId(3L, 8L)).willReturn(true);

        boolean result = postFinder.isDidLikeUser(8L, 3L);

        assertThat(result).isTrue();
        verify(postLikeRepository).existsByUserIdAndPostId(3L, 8L);
    }

    @Test
    @DisplayName("findProxyById delegates to repository")
    void findProxyById_delegatesToRepository() {
        Post proxy = Post.builder().id(55L).build();
        given(postRepository.getReferenceById(55L)).willReturn(proxy);

        Post result = postFinder.findProxyById(55L);

        assertThat(result).isSameAs(proxy);
        verify(postRepository).getReferenceById(55L);
    }
}
