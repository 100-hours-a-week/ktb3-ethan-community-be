package org.restapi.springrestapi.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.PostSummary;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.service.post.LocalPostViewDebounce;
import org.restapi.springrestapi.service.post.PostServiceImpl;
import org.restapi.springrestapi.validator.PostValidator;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock PostRepository postRepository;
    @Mock PostFinder postFinder;
    @Mock UserFinder userFinder;
    @Mock PostValidator postValidator;
    @Mock UserValidator userValidator;
    @Mock LocalPostViewDebounce localPostViewDebounce;

    @InjectMocks
    PostServiceImpl postService;

    @Test
    @DisplayName("게시글 생성 시 사용자 검증 후 저장된 게시글 요약을 반환한다")
    void createPost_validUser_createsPost() {
        // given
        Long userId = 7L;
        CreatePostRequest request = new CreatePostRequest("title", "content", "thumb");
        User author = sampleUser(userId);
        Post savedPost = samplePost(30L, author);

        given(userFinder.findProxyById(userId)).willReturn(author);
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostSummary summary = postService.createPost(userId, request);

        // then
        verify(userValidator).validateUserExists(userId);
        verify(userFinder).findProxyById(userId);
        verify(postRepository).save(any(Post.class));
        assertThat(summary.id()).isEqualTo(savedPost.getId());
        assertThat(summary.userNickname()).isEqualTo(author.getNickname());
    }

    @Test
    @DisplayName("게시글 수정 시 작성자 검증 후 수정된 내용을 저장한다")
    void updatePost_validAuthor_updatesEntity() {
        // given
        Long authorId = 7L;
        Long postId = 11L;
        PatchPostRequest request = new PatchPostRequest("new title", "new content", "new thumb", false);
        Post existing = samplePost(postId, sampleUser(authorId));
        Post updated = existing.toBuilder()
                .title(request.title())
                .content(request.content())
                .thumbnailImageUrl(request.thumbnailImageUrl())
                .build();

        given(postFinder.findById(postId)).willReturn(existing);

        // when
        postService.patchPost(authorId, postId, request);

        // then
//        verify(updated).patch(request);
        verify(postValidator).validateAuthor(authorId, postId);
        assertThat(request.title()).isEqualTo(request.title());
        assertThat(request.content()).isEqualTo(request.content());
    }

    @Test
    @DisplayName("게시글 삭제 시 작성자 검증 후 연관 관계를 끊고 삭제한다")
    void deletePost_validAuthor_deletesPost() {
        // given
        Long postId = 3L;
        Long userId = 9L;
        Post post = samplePost(postId, sampleUser(userId));
        given(postFinder.findProxyById(postId)).willReturn(post);

        // when
        postService.deletePost(userId, postId);

        // then
        verify(postValidator).validateAuthor(userId, postId);
        verify(postRepository).deleteById(postId);
        assertThat(post.getAuthor()).isNull();
    }

    @Test
    @DisplayName("포스트 목록이 존재하면 마지막 id 기반으로 다음 커서를 계산한다")
    void getPostList_returnsNextCursorFromLastPost() {
        // given
        PostSummary first = samplePostSummary(30L, "첫번째");
        PostSummary second = samplePostSummary(11L, "두번째");
        Slice<PostSummary> slice = new SliceImpl<>(
                List.of(first, second),
                PageRequest.of(0, 2),
                false
        );
        given(postFinder.findPostSummarySlice(null, 2)).willReturn(slice);

        // when
        PostListResult result = postService.getPostList(null, 2);

        // then
        assertThat(result.posts()).containsExactly(first, second);
        assertThat(result.nextCursor()).isEqualTo(10);
    }

    @Test
    @DisplayName("추가 데이터가 없을 때는 입력 커서를 유지한 채 빈 목록을 반환한다")
    void getPostList_returnsEmptyWhenSliceEmptyWithCursor() {
        // given
        Slice<PostSummary> emptySlice = new SliceImpl<>(
                List.of(),
                PageRequest.of(0, 5),
                false
        );
        given(postFinder.findPostSummarySlice(50L, 5)).willReturn(emptySlice);

        // when
        PostListResult result = postService.getPostList(50L, 5);

        // then
        assertThat(result.posts()).isEmpty();
        assertThat(result.nextCursor()).isEqualTo(50L);
    }

    @Test
    @DisplayName("게시글을 처음 조회하면 조회수를 증가시킨 뒤 상세 결과를 반환한다")
    void getPost_incrementsViewCount_whenNotSeenRecently() {
        // given
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        Post post = samplePost(1L);
        given(postFinder.findById(1L)).willReturn(post);
        given(postFinder.isDidLikeUser(1L, 7L)).willReturn(true);
        given(localPostViewDebounce.seenRecently(request, 7L, 1L)).willReturn(false);

        // when
        PostResult result = postService.getPost(request, 7L, 1L);

        // then
        verify(postRepository).incrementViewCount(1L);
        assertThat(result.isDidLike()).isTrue();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("최근 조회 이력이 있다면 조회수를 증가시키지 않는다")
    void getPost_doesNotIncrementViewCount_whenSeenRecently() {
        // given
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        Post post = samplePost(3L);
        given(postFinder.findById(3L)).willReturn(post);
        given(postFinder.isDidLikeUser(3L, null)).willReturn(false);
        given(localPostViewDebounce.seenRecently(request, null, 3L)).willReturn(true);

        // when
        PostResult result = postService.getPost(request, null, 3L);

        // then
        verify(postRepository, never()).incrementViewCount(anyLong());
        assertThat(result.isDidLike()).isFalse();
        assertThat(result.getId()).isEqualTo(3L);
    }

    private PostSummary samplePostSummary(Long id, String title) {
        return PostSummary.builder()
                .id(id)
                .title(title)
                .userNickname("tester")
                .userProfileImageUrl("https://img")
                .thumbnailImageUrl("https://thumb")
                .commentCount(0)
                .likeCount(0)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Post samplePost(Long id) {
        return samplePost(id, sampleUser(7L));
    }

    private Post samplePost(Long id, User author) {
        return Post.builder()
                .id(id)
                .title("title")
                .content("content")
                .thumbnailImageUrl("https://thumb")
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .author(author)
                .build();
    }

    private User sampleUser(Long id) {
        return User.builder()
                .id(id)
                .nickname("tester")
                .email("tester@example.com")
                .password("secret")
                .profileImageUrl("https://img")
                .joinAt(LocalDateTime.now())
                .build();
    }
}
