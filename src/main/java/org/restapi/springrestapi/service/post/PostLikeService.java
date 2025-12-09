package org.restapi.springrestapi.service.post;

import lombok.RequiredArgsConstructor;
import org.restapi.springrestapi.dto.post.PatchPostLikeResult;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.PostLike;
import org.restapi.springrestapi.repository.PostLikeRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserFinder userFinder;
    private final PostFinder postFinder;

    public PatchPostLikeResult togglePostLike(Long userId, Long postId) {
        userFinder.existsByIdOrThrow(userId);
		postFinder.existsByIdOrThrow(postId);

        final boolean wasLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);

        int likeCount;
        if (wasLiked) {
            PostLike postLike = postLikeRepository.findByUserIdAndPostId(userId, postId);
            postLikeRepository.delete(postLike);
            postRepository.decreaseLikeCount(postId);

        } else {
            postLikeRepository.save(new PostLike(
				userFinder.findProxyById(userId),
				postFinder.findProxyById(postId)
			));
			postRepository.increaseLikeCount(postId);
        }

        likeCount = postRepository.findLikeCountById(postId).orElse(0);
        return PatchPostLikeResult.from(likeCount, !wasLiked);
    }
}
