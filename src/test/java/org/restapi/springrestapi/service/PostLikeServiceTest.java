package org.restapi.springrestapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.post.PatchPostLikeResult;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.PostLike;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.PostLikeRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.service.post.PostLikeService;
import org.restapi.springrestapi.support.fixture.PostFixture;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.restapi.springrestapi.validator.UserValidator;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock PostRepository postRepository;
    @Mock PostLikeRepository postLikeRepository;
    @Mock
    UserValidator userValidator;
    @Mock UserFinder userFinder;
    @Mock
    PostFinder postFinder;

    @InjectMocks PostLikeService postLikeService;

    @Test
    @DisplayName("이미 좋아요한 경우 좋아요를 취소하고 카운트를 감소시킨다")
    void togglePostLike_alreadyLiked_unlikes() {
        // given
        Long userId = 1L;
        Long postId = 2L;
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(100L));
        PostLike like = new PostLike(UserFixture.persistedUser(userId), post);
        given(postLikeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);
        given(postLikeRepository.findByUserIdAndPostId(userId, postId)).willReturn(like);
        given(postRepository.findLikeCountById(postId)).willReturn(Optional.of(3));

        // when
        PatchPostLikeResult result = postLikeService.togglePostLike(userId, postId);

        // then
        verify(userFinder).existsByIdOrThrow(userId);
        verify(postFinder).existsByIdOrThrow(postId);
        verify(postRepository).decreaseLikeCount(postId);
        verify(postLikeRepository).delete(like);
//        assertThat(result.isLiked()).isFalse();
        assertThat(result.likeCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("좋아요하지 않은 경우 새 레코드를 저장하고 카운트를 증가시킨다")
    void togglePostLike_notLiked_savesLike() {
        // given
        Long userId = 4L;
        Long postId = 5L;
        User user = UserFixture.persistedUser(userId);
        Post post = PostFixture.persistedPost(postId, UserFixture.persistedUser(100L));
        given(postLikeRepository.existsByUserIdAndPostId(userId, postId)).willReturn(false);
        given(userFinder.findProxyById(userId)).willReturn(user);
        given(postFinder.findProxyById(postId)).willReturn(post);
        given(postRepository.findLikeCountById(postId)).willReturn(Optional.of(11));

        // when
        PatchPostLikeResult result = postLikeService.togglePostLike(userId, postId);

        // then
        verify(userFinder).existsByIdOrThrow(userId);
        verify(postFinder).existsByIdOrThrow(postId);
        verify(postRepository).increaseLikeCount(postId);
        verify(postLikeRepository).save(any(PostLike.class));
//        assertThat(result.isLiked()).isTrue();
        assertThat(result.likeCount()).isEqualTo(11);
    }

}
