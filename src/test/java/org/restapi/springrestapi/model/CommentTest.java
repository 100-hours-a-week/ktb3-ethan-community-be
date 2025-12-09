package org.restapi.springrestapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.support.fixture.CommentFixture;
import org.restapi.springrestapi.support.fixture.PostFixture;
import org.restapi.springrestapi.support.fixture.UserFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    User user;
    Post post;
    Comment comment;

    @BeforeEach
    void setUp() {
        user = UserFixture.persistedUser();
        post = PostFixture.persistedPost(user);
        comment = CommentFixture.persistedComment(user, post);
    }

    @Nested
    class Create {
        @Test
        @DisplayName("CreateCommentRequest로 생성하면 필드와 연관관계를 매핑한다")
        void from_validInputs_mapsFieldsAndRelationships() {
            CreateCommentRequest request = new CreateCommentRequest("내용");

            Comment comment = Comment.from(request, user, post);

            assertThat(comment.getContent()).isEqualTo("내용");
            assertThat(comment.getUser()).isEqualTo(user);
            assertThat(comment.getPost()).isEqualTo(post);
            assertThat(user.getComments()).contains(comment);
            assertThat(post.getComments()).contains(comment);
        }

        @Test
        @DisplayName("PrePersist가 호출되면 createdAt과 updatedAt을 채운다")
        void prePersist_setsTimestamps() {
            Comment comment = Comment.builder()
                .content("content")
                .build();

            comment.prePersist();

            assertThat(comment.getCreatedAt()).isNotNull();
            assertThat(comment.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class UpdateContent {
        @Test
        @DisplayName("본문 수정 시 content와 updatedAt을 갱신한다")
        void updateContent_updatesBodyAndTimestamp() {
            LocalDateTime previousUpdatedAt = comment.getUpdatedAt();
            PatchCommentRequest request = new PatchCommentRequest("수정됨");

            comment.updateContent(request);

            assertThat(comment.getContent()).isEqualTo("수정됨");
            assertThat(comment.getUpdatedAt()).isAfter(previousUpdatedAt);
        }
    }

    @Nested
    class ChangeRelationships {
        @Test
        @DisplayName("처음 연관관계를 설정하면 양쪽 컬렉션이 동기화된다")
        void changePost_setsRelationshipWhenUnassigned() {
            Comment orphan = Comment.builder()
                .content("temp")
                .build();

            orphan.changePost(post);
            orphan.changeUser(user);

            assertThat(orphan.getPost()).isEqualTo(post);
            assertThat(orphan.getUser()).isEqualTo(user);
            assertThat(post.getComments()).contains(orphan);
            assertThat(user.getComments()).contains(orphan);
        }

        @Test
        @DisplayName("이미 같은 연관관계가 설정돼 있으면 중복으로 추가하지 않는다")
        void changePost_doesNotDuplicateWhenAlreadyLinked() {
            long postCountBefore = post.getComments().stream().filter(existing -> existing == comment).count();
            long userCountBefore = user.getComments().stream().filter(existing -> existing == comment).count();

            comment.changePost(post);
            comment.changeUser(user);

            long postCountAfter = post.getComments().stream().filter(existing -> existing == comment).count();
            long userCountAfter = user.getComments().stream().filter(existing -> existing == comment).count();

            assertThat(postCountBefore).isEqualTo(1);
            assertThat(userCountBefore).isEqualTo(1);
            assertThat(postCountAfter).isEqualTo(1);
            assertThat(userCountAfter).isEqualTo(1);
        }

        @Test
        @DisplayName("연관관계를 null로 설정하면 양쪽 모두에서 제거된다")
        void changePost_toNull_detachesFromBothSides() {
            comment.changePost(null);
            comment.changeUser(null);

            assertThat(comment.getPost()).isNull();
            assertThat(comment.getUser()).isNull();
            assertThat(post.getComments()).doesNotContain(comment);
            assertThat(user.getComments()).doesNotContain(comment);
        }
    }
}
