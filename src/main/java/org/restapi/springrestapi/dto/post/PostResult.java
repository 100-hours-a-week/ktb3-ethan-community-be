package org.restapi.springrestapi.dto.post;

import java.time.LocalDateTime;
import org.restapi.springrestapi.model.Post;

import lombok.Builder;

@Builder
public record PostResult (
	Long id,
    Long userId,
    String userNickname,
    String userProfileImageUrl,

	String title,
	String content,
    String thumbnailImageUrl,
	boolean didLike,

	int likeCount,
	int commentCount,
	int viewCount,

	LocalDateTime createdAt
) {
	public static PostResult from(Post post, boolean didLike) {
		return PostResult.builder()
			.id(post.getId())
            .userId(post.getAuthor().getId())
            .userNickname(post.getAuthor().getNickname())
            .userProfileImageUrl(post.getAuthor().getProfileImageUrl())
			.title(post.getTitle())
			.content(post.getContent())
            .thumbnailImageUrl(post.getThumbnailImageUrl())
			.likeCount(post.getLikeCount())
			.commentCount(post.getCommentCount())
			.viewCount(post.getViewCount())
			.createdAt(post.getCreatedAt())
            .didLike(didLike)
			.build();
	}
}
