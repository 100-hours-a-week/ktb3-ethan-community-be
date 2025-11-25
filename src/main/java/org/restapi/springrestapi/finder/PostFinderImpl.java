package org.restapi.springrestapi.finder;

import org.restapi.springrestapi.dto.post.PostSummary;
import org.restapi.springrestapi.exception.AppException;
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
public class PostFinderImpl implements PostFinder {
	private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

	@Override
	public Post findById(Long id) {
		return postRepository.findById(id)
			.orElseThrow(() -> new AppException(PostErrorCode.POST_NOT_FOUND));
	}

    @Override
    public Post findProxyById(Long id) {
        return postRepository.getReferenceById(id);
    }

    @Override
    public Slice<PostSummary> findPostSummarySlice(Long cursor, int limit) {

        // check limit range
        final int SIZE = Math.min(Math.max(limit, 1), 10);
        if (cursor == null) {
            return postRepository.findSlice(PageRequest.of(0, SIZE));
        }
        return postRepository.findSlice(cursor, PageRequest.of(0, SIZE));
    }

	@Override
	public boolean existsById(Long id) {
		return postRepository.existsById(id);
	}

    @Override
    public boolean existsByIdAndAuthorId(Long postId, Long authorId) {
        return postRepository.existsByIdAndAuthorId(postId, authorId);
    }

    @Override
    public boolean isDidLikeUser(Long postId, Long userIdOrNull) {
        if (userIdOrNull == null) {
            return false;
        }
        return postLikeRepository.existsByUserIdAndPostId(userIdOrNull, postId);
    }
}
