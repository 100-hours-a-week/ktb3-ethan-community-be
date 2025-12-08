package org.restapi.springrestapi.dto.post;

import java.util.List;

import lombok.Builder;

@Builder
public record PostListResult(
	List<PostResult> posts,
	long nextCursor
) {
	public static PostListResult from(List<PostResult> posts, long nextCursor) {
		return PostListResult.builder()
			.posts(posts)
			.nextCursor(nextCursor)
			.build();
	}
}
