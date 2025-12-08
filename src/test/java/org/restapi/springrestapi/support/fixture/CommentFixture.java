package org.restapi.springrestapi.support.fixture;

import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.model.User;

import java.time.LocalDateTime;

public final class CommentFixture {

    private CommentFixture() {
    }

    public static Comment persistedComment(User user, Post post) {
        Comment comment = Comment.builder()
            .content("기존 댓글")
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now().minusHours(5))
            .build();

        if (user != null) {
            comment.changeUser(user);
        }
        if (post != null) {
            comment.changePost(post);
        }
        return comment;
    }
}
