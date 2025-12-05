package org.restapi.springrestapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PostTest {

    User author;

    @BeforeEach
    void setUp() {
        author = sampleAuthor(1L);
    }

    private User sampleAuthor(Long id) {
        return User.builder()
                .id(id)
                .nickname("author" + id)
                .email("author" + id + "@test.com")
                .profileImageUrl("https://img/" + id)
                .build();
    }

    @Test
    @DisplayName("게시글을 수정(patch)하면 수정일(updatedAt)이 현재 시간으로 갱신된다")
    void patch_updates_updatedAt() {
        // given
        LocalDateTime pastTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        Post post = Post.builder()
                .title("기존 제목")
                .content("기존 내용")
                .updatedAt(pastTime)
                .build();

        LocalDateTime beforePatchPostTime = LocalDateTime.now();
        PatchPostRequest request = new PatchPostRequest(null, null, null, false);

        // when
        post.patch(request);

        // then
        assertThat(post.getUpdatedAt()).isNotEqualTo(pastTime);
        assertThat(post.getUpdatedAt()).isAfter(beforePatchPostTime);
    }

    @Test
    @DisplayName("patch 호출 시 전달된 필드만 변경하고 썸네일 제거 플래그면 null로 설정한다")
    void patch_updatesSelectedFields() {
        // given
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(1);
        Post post = Post.builder()
                .title("old title")
                .content("old content")
                .thumbnailImageUrl("thumb")
                .author(author)
                .build();

        PatchPostRequest request = new PatchPostRequest("new title", null, null, true);

        // when
        post.patch(request);

        // then
        assertThat(post.getTitle()).isEqualTo("new title");
        assertThat(post.getContent()).isEqualTo("old content");
        assertThat(post.getThumbnailImageUrl()).isNull();
        assertThat(post.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("patch에서 removeThumbnail=false이고 썸네일이 null이면 기존 값을 유지한다")
    void patch_keepsThumbnailWhenNullWithoutRemoveFlag() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .thumbnailImageUrl("thumb")
                .author(author)
                .build();

        PatchPostRequest request = new PatchPostRequest(null, "updated content", null, false);

        // when
        post.patch(request);

        // then
        assertThat(post.getThumbnailImageUrl()).isEqualTo("thumb");
        assertThat(post.getContent()).isEqualTo("updated content");
    }

    @Test
    @DisplayName("changeAuthor는 게시글 등록 시 author와 author.posts 양방향 관계를 세팅한다")
    void changeAuthor_attachs_post_to_author() {
        // given
        User author = sampleAuthor(99L);
        Post post = Post.builder().build(); // 아직 작성자 없음

        // when
        post.changeAuthor(author);

        // then
        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(author.getPosts()).contains(post);
    }

    @Test
    @DisplayName("changeAuthor(null)는 기존 작성자의 posts 리스트에서 제거하고 author를 null로 만든다")
    void changeAuthor_detaches_post_from_author() {
        // given
        User author = sampleAuthor(99L);
        Post post = Post.builder()
                .author(author)
                .build();

        author.getPosts().add(post); // 양방향 세팅

        // when
        post.changeAuthor(null);

        // then
        assertThat(post.getAuthor()).isNull();
        assertThat(author.getPosts()).doesNotContain(post);
    }

}
