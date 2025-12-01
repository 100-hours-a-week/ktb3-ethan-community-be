package org.restapi.springrestapi.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.restapi.springrestapi.dto.comment.RegisterCommentRequest;

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

    @Column(nullable = false)
	private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

	public static Comment from(RegisterCommentRequest command, User user) {
		return Comment.builder()
                .content(command.content())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
			    .build();
	}

	public void updateContent(String newContent) {
		if (newContent != null && !newContent.equals(this.content)) {
			this.content = newContent;
            this.updatedAt = LocalDateTime.now();
		}
	}

    public void changePost(Post newPost) {
        if (this.post != null) {
            this.post.getComments().remove(this);
        }
        this.post = newPost;
        if (this.post != null) {
            this.post.getComments().add(this);
        }
    }
}
