package org.restapi.springrestapi.finder;

import org.restapi.springrestapi.dto.post.PostSummary;
import org.restapi.springrestapi.model.Post;
import org.springframework.data.domain.Slice;

public interface PostFinder {
	Post findById(Long id);
    Post findProxyById(Long id);
	Slice<PostSummary> findPostSummarySlice(Long cursor, int limit);
	boolean existsById(Long id);
	boolean existsByIdAndAuthorId(Long postId, Long authorId);
    boolean isDidLikeUser(Long postId, Long userIdOrNull);
}
