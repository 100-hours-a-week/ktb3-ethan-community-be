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
import org.restapi.springrestapi.dto.post.RegisterPostRequest;

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


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /*
    constructor=
    - from(RegisterPostRequest)
    - from(PatchPostRequest, Post)
     */
    public static Post from(RegisterPostRequest command) {
		return Post.builder()
                .title(command.title())
                .content(command.content())
                .thumbnailImageUrl(command.thumbnailImageUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
	}

	public static Post from(PatchPostRequest command, Post prevPost) {
		return prevPost.toBuilder()
                .title(command.title())
                .content(command.content())
                .thumbnailImageUrl(command.thumbnailImageUrl())
                .updatedAt(LocalDateTime.now())
                .build();
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
