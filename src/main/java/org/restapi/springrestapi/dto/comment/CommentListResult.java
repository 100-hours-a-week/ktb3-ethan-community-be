package org.restapi.springrestapi.dto.comment;

import java.util.List;

import lombok.Builder;

@Builder
public record CommentListResult(
	List<CommentResult> comments,
	int nextCursor
) {
	public static CommentListResult from(List<CommentResult> comments, int nextCursor) {
		return CommentListResult.builder()
			.comments(comments)
			.nextCursor(nextCursor)
			.build();
	}
    public static CommentListResult empty() {
        return CommentListResult.builder()
                .comments(null)
                .nextCursor(0)
                .build();
    }
}
