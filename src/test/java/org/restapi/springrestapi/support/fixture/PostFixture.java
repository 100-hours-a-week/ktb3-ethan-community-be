package org.restapi.springrestapi.support.fixture;

import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;

import java.time.LocalDateTime;

public final class PostFixture {
    private PostFixture() {}

    public static Post persistedPost(User author) {
        Post post = Post.builder()
            .title("old title")
            .content("old content")
            .thumbnailImageUrl("http://thumb")
            .createdAt(LocalDateTime.now().minusDays(2))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .build();

        if (author != null) {
            post.changeAuthor(author);
        }
        return post;
    }

    public static Post persistedPost(Long id, User author) {
        Post post = persistedPost(author).toBuilder()
            .id(id)
            .build();
        if (author != null) {
            post.changeAuthor(author);
        }
        return post;
    }
}
