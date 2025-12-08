package org.restapi.springrestapi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.restapi.springrestapi.common.annotation.ValidPostTitle;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.CreatePostRequest;

import lombok.Builder;
import lombok.Getter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(nullable = false)
    @ValidPostTitle
	private String title;

    @Column(nullable = false)
    @NotBlank
    private String content;

    private String thumbnailImageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 집계 컬럼
	private int likeCount;
    private int viewCount;
	private int commentCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();


    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Post from(CreatePostRequest req, User author) {
		Post post =  Post.builder()
                .title(req.title())
                .content(req.content())
                .thumbnailImageUrl(req.thumbnailImageUrl())
                .build();

		post.changeAuthor(author);
		return post;
	}

    public void update(PatchPostRequest req) {
        if (req.title() != null) {
            this.title = req.title();
        }
        if (req.content() != null) {
            this.content = req.content();
        }
        if (req.removeThumbnailImage()) {
            this.thumbnailImageUrl = null;
        } else if (req.thumbnailImageUrl() != null) {
            this.thumbnailImageUrl = req.thumbnailImageUrl();
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void changeAuthor(User newAuthor) {
        if (this.author != null) {
            this.author.getPosts().remove(this);
        }
        this.author = newAuthor;
        if (this.author != null) {
            this.author.getPosts().add(this);
        }
    }
}
