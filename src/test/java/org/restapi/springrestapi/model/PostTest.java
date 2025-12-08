package org.restapi.springrestapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.support.fixture.PostFixture;
import org.restapi.springrestapi.support.fixture.UserFixture;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostTest {

    User author;

    @BeforeEach
    void setUp() {
        author = UserFixture.persistedUser();
    }

    @Nested
    @DisplayName("생성 테스트")
    class Create {
        @Test
        @DisplayName("빌더로 생성한 Post는 comments 컬렉션이 비어 있지만 null이 아니다")
        void builder_initializesCommentsCollection() {
			Post post = Post.builder().build();

            assertThat(post.getComments()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("CreatePostRequest로부터 생성되면 필드와 author 연관관계가 매핑된다")
        void from_validInputs_mapsFieldsAndAuthor() {
            // given
            CreatePostRequest request = new CreatePostRequest(
                "제목",
                "본문",
                "http://thumb"
            );

            // when
            Post mapped = Post.from(request, author);

            // then
            assertThat(mapped.getTitle()).isEqualTo(request.title());
            assertThat(mapped.getContent()).isEqualTo(request.content());
            assertThat(mapped.getThumbnailImageUrl()).isEqualTo(request.thumbnailImageUrl());
            assertThat(mapped.getAuthor()).isEqualTo(author);
            assertThat(author.getPosts()).contains(mapped);
        }

        @Test
        @DisplayName("CreatePostRequest가 null이면 NullPointerException을 던진다")
        void from_nullRequest_throwsException() {
            assertThatThrownBy(() -> Post.from(null, author))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Update {
        @Test
        @DisplayName("제목/내용/썸네일 수정 요청 시 해당 필드와 updatedAt이 갱신된다")
        void update_overwritesTitleContentThumbnailAndTimestamp() {
            // given
            Post post = PostFixture.persistedPost(author);
            LocalDateTime previousUpdatedAt = post.getUpdatedAt();
            PatchPostRequest request = new PatchPostRequest(
                "새 제목",
                "새 본문",
                "http://new-thumb",
                false
            );

            // when
            post.update(request);

            // then
            assertThat(post.getTitle()).isEqualTo("새 제목");
            assertThat(post.getContent()).isEqualTo("새 본문");
            assertThat(post.getThumbnailImageUrl()).isEqualTo("http://new-thumb");
            assertThat(post.getUpdatedAt()).isAfter(previousUpdatedAt);
        }

        @Test
        @DisplayName("removeThumbnailImage=true면 썸네일 경로가 제거되고 null로 유지된다")
        void update_removeThumbnailImage_setsNull() {
            // given
            Post post = PostFixture.persistedPost(author);
            PatchPostRequest request = new PatchPostRequest(
                null,
                null,
                null,
                true
            );

            // when
            post.update(request);

            // then
            assertThat(post.getThumbnailImageUrl()).isNull();
        }

        @Test
        @DisplayName("썸네일 새 값이 null이고 removeThumbnailImage=false면 기존 값을 유지한다")
        void update_nullThumbnail_keepsPrevious() {
            // given
            Post post = PostFixture.persistedPost(author);
            String previousThumbnail = post.getThumbnailImageUrl();
            PatchPostRequest request = new PatchPostRequest(
                null,
                null,
                null,
                false
            );

            // when
            post.update(request);

            // then
            assertThat(post.getThumbnailImageUrl()).isEqualTo(previousThumbnail);
        }
    }

    @Nested
    class ChangeAuthor {
		@Test
		@DisplayName("작성자를 null로 변경하면 양방향 연관관계가 끊긴다")
		void changeAuthor_toNull_detachesPost() {
			Post post = PostFixture.persistedPost(author);
			post.changeAuthor(author);

			post.changeAuthor(null);

			assertThat(post.getAuthor()).isNull();
			assertThat(author.getPosts()).doesNotContain(post);
		}
    }
}
