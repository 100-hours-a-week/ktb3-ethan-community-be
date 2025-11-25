package org.restapi.springrestapi.dto.comment;

import java.time.LocalDateTime;

import org.restapi.springrestapi.model.Comment;

import lombok.Builder;

@Builder
public record CommentResult(
	Long id,
    String content,
	LocalDateTime createAt,
	Long userId
) {
	public static CommentResult from(Comment comment) {
		return CommentResult.builder()
			    .id(comment.getId())
                .content(comment.getContent())
                .createAt(comment.getCreatedAt())
                .userId(comment.getUser().getId())
                .build();
	}
}
