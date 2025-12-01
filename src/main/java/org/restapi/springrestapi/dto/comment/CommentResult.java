package org.restapi.springrestapi.dto.comment;

import java.time.LocalDateTime;

import org.restapi.springrestapi.model.Comment;

import lombok.Builder;

@Builder
public record CommentResult(
	Long id,
    Long userId,
    String userNickname,
    String userProfileImageUrl,
    String content,
	LocalDateTime createAt
) {
	public static CommentResult from(Comment comment) {
		return CommentResult.builder()
			    .id(comment.getId())
                .userId(comment.getUser().getId())
                .userNickname(comment.getUser().getNickname())
                .userProfileImageUrl(comment.getUser().getProfileImageUrl())
                .content(comment.getContent())
                .createAt(comment.getCreatedAt())
                .build();
	}
}
