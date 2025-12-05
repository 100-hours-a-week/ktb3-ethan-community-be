package org.restapi.springrestapi.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {

    @Test
    @DisplayName("PostLike 생성 시 Post.likes 컬렉션에 추가된다")
    void constructor_addsToPostLikes() {
        // given
        Post post = samplePost(1L);
        User user = sampleUser(1L);

        // when
        PostLike like = new PostLike(user, post);

        // then
        assertThat(post.getLikes()).contains(like);
        assertThat(like.getPost()).isEqualTo(post);
        assertThat(like.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("unLike 호출 시 Post.likes 컬렉션에서 제거된다")
    void unLike_removesFromPostLikes() {
        // given
        Post post = samplePost(2L);
        PostLike like = new PostLike(sampleUser(2L), post);
        assertThat(post.getLikes()).contains(like);

        // when
        like.unLike();

        // then
        assertThat(post.getLikes()).doesNotContain(like);
    }

    private User sampleUser(Long id) {
        return User.builder()
                .id(id)
                .nickname("user" + id)
                .email("user" + id + "@test.com")
                .password("pw")
                .build();
    }

    private Post samplePost(Long id) {
        return Post.builder()
                .id(id)
                .title("title")
                .content("content")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .author(sampleUser(100L))
                .build();
    }
}
