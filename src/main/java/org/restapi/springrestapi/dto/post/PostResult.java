package org.restapi.springrestapi.dto.post;

import java.time.LocalDateTime;
import org.restapi.springrestapi.model.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResult {
	private Long id;
    private Long userId;
    private String userNickName;
    private String userProfileImageUrl;
	private String title;
	private String content;
    private String thumbnailImageUrl;
	private boolean didLike;
	private int likeCount;
	private int commentCount;
	private int viewCount;
	private LocalDateTime createdAt;

	public static PostResult from(Post post, boolean didLike) {
		return PostResult.builder()
			.id(post.getId())
            .userId(post.getAuthor().getId())
            .userNickName(post.getAuthor().getNickname())
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
    public static PostResult from(Post post) {
        return PostResult.from(post, false);
    }
}
