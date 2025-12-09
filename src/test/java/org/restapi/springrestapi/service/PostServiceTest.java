package org.restapi.springrestapi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.PostErrorCode;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.service.post.LocalPostViewDebounce;
import org.restapi.springrestapi.service.post.PostService;
import org.restapi.springrestapi.support.fixture.PostFixture;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    PostService postService;

    @Mock PostRepository postRepository;
    @Mock PostFinder postFinder;
    @Mock LocalPostViewDebounce localPostViewDebounce;
    @Mock UserFinder userFinder;

    @Test
    @DisplayName("게시글 작성 시 작성자 정보와 요청 본문으로 jwt저장 후 DTO를 반환한다")
    void createPost_persistsEntityAndReturnsResult() {
        User author = UserFixture.persistedUser(7L);
        CreatePostRequest request = new CreatePostRequest("제목", "본문", "thumb.jpg");
        Post savedPost = PostFixture.persistedPost(31L, author);
        given(userFinder.findByIdOrAuthThrow(author.getId())).willReturn(author);
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        PostResult result = postService.createPost(author.getId(), request);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post toPersist = captor.getValue();
        assertThat(toPersist.getAuthor()).isEqualTo(author);
        assertThat(toPersist.getTitle()).isEqualTo(request.title());
        assertThat(result.id()).isEqualTo(savedPost.getId());
        assertThat(result.didLike()).isFalse();
    }

    @Test
    @DisplayName("게시글 목록 조회 시 Slice 결과를 그대로 반환하고 다음 커서를 계산한다")
    void getPostList_buildsNextCursorFromLastElement() {
        PostResult first = samplePostResult(30L, "첫번째");
        PostResult second = samplePostResult(17L, "두번째");
        Slice<PostResult> slice = new SliceImpl<>(List.of(first, second), PageRequest.of(0, 2), false);
        given(postFinder.findPostSummarySlice(null, 2)).willReturn(slice);

        PostListResult result = postService.getPostList(null, 2);

        assertThat(result.posts()).containsExactly(first, second);
        assertThat(result.nextCursor()).isEqualTo(16);
    }

    @Test
    @DisplayName("커서 기반 요청에서 게시글이 없으면 빈 리스트와 기존 커서를 반환한다")
    void getPostList_returnsEmptyListWhenSliceIsEmpty() {
        Slice<PostResult> emptySlice = new SliceImpl<>(List.of(), PageRequest.of(0, 5), false);
        given(postFinder.findPostSummarySlice(50L, 5)).willReturn(emptySlice);

        PostListResult result = postService.getPostList(50L, 5);

        assertThat(result.posts()).isEmpty();
        assertThat(result.nextCursor()).isEqualTo(50L);
    }

    @Test
    @DisplayName("게시글 상세 조회 시 최근 조회 이력이 없으면 조회수를 증가시킨다")
    void getPost_incrementsViewCountWhenNotSeenRecently() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Long postId = 10L;
        Long userId = 3L;
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(userId));
        given(postFinder.findByIdOrThrow(postId)).willReturn(post);
        given(postFinder.isDidLikeUser(postId, userId)).willReturn(true);
        given(localPostViewDebounce.seenRecently(request, userId, postId)).willReturn(false);

        PostResult result = postService.getPost(request, userId, postId);

        verify(postFinder).findByIdOrThrow(postId);
        verify(postFinder).isDidLikeUser(postId, userId);
        verify(localPostViewDebounce).seenRecently(request, userId, postId);
        verify(postRepository).incrementViewCount(postId);
        assertThat(result.didLike()).isTrue();
        assertThat(result.id()).isEqualTo(postId);
    }

    @Test
    @DisplayName("최근 조회한 게시글은 조회수를 증가시키지 않는다")
    void getPost_doesNotIncrementViewCountWhenSeenRecently() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Long postId = 4L;
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(10L));
        given(postFinder.findByIdOrThrow(postId)).willReturn(post);
        given(postFinder.isDidLikeUser(postId, null)).willReturn(false);
        given(localPostViewDebounce.seenRecently(request, null, postId)).willReturn(true);

        PostResult result = postService.getPost(request, null, postId);

        verify(postRepository, never()).incrementViewCount(anyLong());
        assertThat(result.didLike()).isFalse();
    }

    @Test
    @DisplayName("작성자가 맞으면 게시글을 수정한다")
    void updatePost_updatesWhenAuthorMatches() {
        Long authorId = 5L;
        Long postId = 22L;
        PatchPostRequest request = new PatchPostRequest("새 제목", "새 본문", "", true);
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(authorId));
        given(postFinder.findByIdOrThrow(postId)).willReturn(post);

        postService.updatePost(authorId, postId, request);

        assertThat(post.getTitle()).isEqualTo(request.title());
        assertThat(post.getContent()).isEqualTo(request.content());
        assertThat(post.getThumbnailImageUrl()).isNull();
    }

    @Test
    @DisplayName("작성자가 아니면 게시글 수정 시 예외가 발생한다")
    void updatePost_throwsWhenUserIsNotAuthor() {
        Long postId = 22L;
        PatchPostRequest request = new PatchPostRequest("새 제목", null, null, false);
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(999L));
        given(postFinder.findByIdOrThrow(postId)).willReturn(post);

        assertThatThrownBy(() -> postService.updatePost(1L, postId, request))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(PostErrorCode.NOT_POST_OWNER));
    }

    @Test
    @DisplayName("작성자가 맞으면 게시글 삭제를 위임한다")
    void deletePost_deletesWhenAuthorMatches() {
        Long postId = 8L;
        Long authorId = 2L;
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(authorId));
        given(postFinder.findByIdOrThrow(postId)).willReturn(post);

        postService.deletePost(authorId, postId);

        verify(postRepository).deleteById(postId);
    }

    @Test
    @DisplayName("작성자가 아니면 게시글 삭제 시 예외가 발생한다")
    void deletePost_throwsWhenUserIsNotAuthor() {
        Long postId = 8L;
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(10L));
        given(postFinder.findByIdOrThrow(postId)).willReturn(post);

        assertThatThrownBy(() -> postService.deletePost(3L, postId))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(PostErrorCode.NOT_POST_OWNER));
        verify(postRepository, never()).deleteById(anyLong());
    }

    private PostResult samplePostResult(Long id, String title) {
        return PostResult.builder()
            .id(id)
            .userId(1L)
            .userNickname("tester")
            .userProfileImageUrl("https://img")
            .title(title)
            .content("content")
            .thumbnailImageUrl("thumb")
            .likeCount(0)
            .commentCount(0)
            .viewCount(0)
            .createdAt(LocalDateTime.now())
            .didLike(false)
            .build();
    }
}
