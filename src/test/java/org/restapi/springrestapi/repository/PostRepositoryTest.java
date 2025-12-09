package org.restapi.springrestapi.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired PostRepository postRepository;
    @Autowired TestEntityManager entityManager;

    @Test
    @DisplayName("조회 수 증가 쿼리가 누적된다")
    void incrementViewCount_updatesColumn() {
        Post post = persistPost("조회");

        postRepository.incrementViewCount(post.getId());
        entityManager.flush();
        entityManager.clear();

        Post updated = entityManager.find(Post.class, post.getId());
        assertThat(updated.getViewCount()).isEqualTo(post.getViewCount() + 1);
    }

    @Test
    @DisplayName("댓글 수 증감 쿼리가 누적된다")
    void increaseAndDecreaseCommentCount() {
        Post post = persistPost("댓글");

        postRepository.increaseCommentCount(post.getId());
        postRepository.increaseCommentCount(post.getId());
        postRepository.decreaseCommentCount(post.getId());
        entityManager.flush();
        entityManager.clear();

        Post updated = entityManager.find(Post.class, post.getId());
        assertThat(updated.getCommentCount()).isEqualTo(post.getCommentCount() + 1);
    }

    @Test
    @DisplayName("좋아요 수 증감과 조회 메서드가 정상 동작한다")
    void updateLikeCountAndFind() {
        Post post = persistPost("좋아요");

        postRepository.increaseLikeCount(post.getId());
        postRepository.increaseLikeCount(post.getId());
        postRepository.decreaseLikeCount(post.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(postRepository.findLikeCountById(post.getId()))
            .hasValue(post.getLikeCount() + 1);
    }

    private Post persistPost(String title) {
        User author = User.builder()
            .nickname("tester-" + title)
            .email(title + "@test.com")
            .password("Password1!")
            .build();
        entityManager.persist(author);

        Post post = Post.builder()
            .title(title)
            .content("content-" + title)
            .thumbnailImageUrl("https://img/"+title)
            .author(author)
            .likeCount(0)
            .commentCount(0)
            .viewCount(0)
            .build();
        entityManager.persist(post);
        entityManager.flush();
        return post;
    }
}
