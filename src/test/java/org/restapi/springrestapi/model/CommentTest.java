package org.restapi.springrestapi.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    @DisplayName("updateContent는 내용이 null이 아닐 떄 수정하고 updatedAt을 갱신한다")
    void updateContent_updatesOnlyWhenChanged() {
        // given
        LocalDateTime previousUpdatedAt = LocalDateTime.now();
        Comment comment = Comment.builder()
                .content("old")
                .updatedAt(previousUpdatedAt)
                .user(sampleUser(1L))
                .build();

        // when
        comment.updateContent("new");

        // then
        assertThat(comment.getContent()).isEqualTo("new");
        assertThat(comment.getUpdatedAt()).isAfter(previousUpdatedAt);
    }

    @Test
    @DisplayName("from은 content를 설정하고, user와 post 양방향 연관관계를 설정한다")
    void from_sets_content_and_associations() {
        // given
        User user = sampleUser(1L);
        Post post = samplePost(10L);
        CreateCommentRequest request = new CreateCommentRequest("test comment");

        // when
        Comment comment = Comment.from(request, user, post);

        // then
        // content
        assertThat(comment.getContent()).isEqualTo("test comment");

        // user 연관관계
        assertThat(comment.getUser()).isEqualTo(user);
        assertThat(user.getComments()).contains(comment);

        // post 연관관계
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(post.getComments()).contains(comment);
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
