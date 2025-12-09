package org.restapi.springrestapi.dto.post;

import java.time.LocalDateTime;

public record PostSummaryProjection(
        Long postId,
        String title,
        String content,
        String thumbnailImageUrl,
        int likeCount,
        int commentCount,
        int viewCount,
        LocalDateTime createdAt,
        Long authorId,
        String authorNickname,
        String authorProfileImageUrl
) {
}
