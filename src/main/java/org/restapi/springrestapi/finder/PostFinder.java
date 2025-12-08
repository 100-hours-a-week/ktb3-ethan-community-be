package org.restapi.springrestapi.finder;

import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.CommentErrorCode;
import org.restapi.springrestapi.exception.code.PostErrorCode;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.repository.PostLikeRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFinder {
	private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public Post findProxyById(Long id) {
        return postRepository.getReferenceById(id);
    }

    public Post findByIdOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new AppException(PostErrorCode.POST_NOT_FOUND));
    }

    public Slice<PostResult> findPostSummarySlice(Long cursor, int limit) {

        // check limit range
        final int SIZE = Math.min(Math.max(limit, 1), 10);
        if (cursor == null) {
            return postRepository.findSlice(PageRequest.of(0, SIZE));
        }
        return postRepository.findSlice(cursor, PageRequest.of(0, SIZE));
    }

	public void existsByIdOrThrow(Long id) {
		if (!postRepository.existsById(id)) {
			throw new AppException(CommentErrorCode.COMMENT_NOT_FOUND);
		}
	}

	public boolean isDidLikeUser(Long postId, Long userIdOrNull) {
		if (userIdOrNull == null) {
			return false;
		}
		return postLikeRepository.existsByUserIdAndPostId(userIdOrNull, postId);
	}
}
