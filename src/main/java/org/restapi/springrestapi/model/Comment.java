package org.restapi.springrestapi.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;

import lombok.Builder;
import lombok.Getter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(nullable = false)
	private String content;

    @Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

	public static Comment from(CreateCommentRequest command, User user, Post post) {
        Comment comment = Comment.builder()
                .content(command.content())
                .build();

        comment.changeUser(user);
        comment.changePost(post);
        return comment;
	}

	public void updateContent(PatchCommentRequest req) {
		this.content = req.content();
		this.updatedAt = LocalDateTime.now();
	}

    public void changePost(Post newPost) {
        // 기존 연관관계 제거
        if (this.post != null) {
            this.post.getComments().remove(this);
        }

        // 새로운 연관관계 설정
        this.post = newPost;

        if (newPost != null && !newPost.getComments().contains(this)) {
            newPost.getComments().add(this);
        }
    }

    public void changeUser(User newUser) {
        // 기존 연관관계 제거
        if (this.user != null) {
            this.user.getComments().remove(this);
        }

        // 새로운 연관관계 설정
        this.user = newUser;

        if (newUser != null && !newUser.getComments().contains(this)) {
            newUser.getComments().add(this);
        }
    }
}
