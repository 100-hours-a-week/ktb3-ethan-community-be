package org.restapi.springrestapi.dto.comment;

import java.time.LocalDateTime;

public record CommentSummaryProjection(
    Long id,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long userId,
    String userNickname,
    String userProfileImageUrl
) {}
