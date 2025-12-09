package org.restapi.springrestapi.finder;


import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.CommentErrorCode;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.repository.CommentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentFinder {
	private final CommentRepository commentRepository;

	public Comment findByIdOrThrow(Long id) {
		return commentRepository.findById(id)
			.orElseThrow(() -> new AppException(CommentErrorCode.COMMENT_NOT_FOUND));
	}

    public Slice<Comment> findCommentSlice(Long postId, Long cursor, int limit) {
        final int SIZE = Math.max(Math.max(limit, 1), 10);

        if (cursor == null) {
            return commentRepository.findSlice(postId, PageRequest.of(0, SIZE));
        }
        return commentRepository.findSlice(postId, cursor, PageRequest.of(0, SIZE));
    }
}
