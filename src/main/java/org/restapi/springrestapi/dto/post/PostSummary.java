package org.restapi.springrestapi.dto.post;

import java.time.LocalDateTime;

import org.restapi.springrestapi.model.Post;

import lombok.Builder;

@Builder(toBuilder = true)
public record PostSummary(
	Long id,
    String userProfileImageUrl,
    String userNickname,
	String title,
    String thumbnailImageUrl,
	int likeCount,
    int commentCount,
    int viewCount,
	LocalDateTime createdAt
) {
    public static PostSummary from(Post post) {
        return base(post);
    }

    private static PostSummary base(Post post) {
        return PostSummary.builder()
                .id(post.getId())
                .userProfileImageUrl(post.getAuthor().getProfileImageUrl())
                .userNickname(post.getAuthor().getNickname())
                .title(post.getTitle())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

}
