package org.restapi.springrestapi.finder;

import org.restapi.springrestapi.model.Comment;
import org.springframework.data.domain.Slice;

public interface CommentFinder {
	Comment findById(Long id);
	Slice<Comment> findCommentSlice(Long postId, Long cursor, int limit);
	boolean existsById(Long id);
    boolean existsByIdAndUserId(Long id, Long userId);
}
