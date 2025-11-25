package org.restapi.springrestapi.dto.post;

import lombok.Builder;

@Builder
public record PatchPostLikeResult(
	int likeCount,
	boolean didLike
) {
	public static PatchPostLikeResult from(int likeCount, boolean didLike) {
		return PatchPostLikeResult.builder()
			.likeCount(likeCount)
			.didLike(didLike)
			.build();
	}
}
