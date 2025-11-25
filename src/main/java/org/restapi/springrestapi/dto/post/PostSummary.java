package org.restapi.springrestapi.dto.post;

import java.time.LocalDateTime;

import org.restapi.springrestapi.dto.user.SimpleUserInfo;
import org.restapi.springrestapi.model.Post;

import lombok.Builder;

@Builder(toBuilder = true)
public record PostSummary(
	Long id,
	Long userId,
    String authorNickname,
	String title,
    String content,
    String thumbnailImageUrl,
	int likeCount,
    int commentCount,
    int viewCount,
	LocalDateTime createdAt
) {
    public static PostSummary from(Post post) {
        return base(post);
    }

    public static PostSummary from(PostSummary post, SimpleUserInfo userInfo) {
        return post.toBuilder().authorNickname(userInfo.nickname()).build();
    }

    private static PostSummary base(Post post) {
        return PostSummary.builder()
                .id(post.getId())
                .userId(post.getAuthor().getId())
                .title(post.getTitle())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

}
