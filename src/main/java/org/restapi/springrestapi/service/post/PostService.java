package org.restapi.springrestapi.service.post;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.PostErrorCode;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.repository.PostRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
	private final PostRepository postRepository;

	private final PostFinder postFinder;
    private final LocalPostViewDebounce localPostViewDebounce;

	public PostResult createPost(User author, CreatePostRequest req) {
		return PostResult.from(postRepository.save(Post.from(req, author)), false);
	}

    public PostListResult getPostList(Long cursor, int limit) {
        List<PostResult> postList = postFinder.findPostSummarySlice(cursor, limit).getContent();

        if (postList.isEmpty() && cursor != null) {
            return PostListResult.from(List.of(), cursor);
        }

        return PostListResult.from(postList, calcNextCursor(postList));
    }

	private int calcNextCursor(List<PostResult> postList) {
		long lastIdDesc = postList.get(postList.size() - 1).id();
		return (int) Math.max(lastIdDesc - 1, 1);
	}

    public PostResult getPost(HttpServletRequest req, Long userIdOrNull, Long id) {
        Post post = postFinder.findByIdOrThrow(id);

        final boolean didLike = postFinder.isDidLikeUser(id, userIdOrNull);
        if (!localPostViewDebounce.seenRecently(req, userIdOrNull, id)) {
            postRepository.incrementViewCount(id);
        }

        return PostResult.from(post, didLike);
    }

	public void updatePost(Long userId, Long id, PatchPostRequest req) {
		Post post = postFinder.findByIdOrThrow(id);

		validatePermission(post, userId);

        post.update(req);
	}

	public void deletePost(Long userId, Long postId) {
		Post post = postFinder.findByIdOrThrow(postId);

		validatePermission(post, userId);

		postRepository.deleteById(postId);
	}

	private void validatePermission(Post post, Long authorId) {
		if (!post.getAuthor().getId().equals(authorId)) {
			throw new AppException(PostErrorCode.NOT_POST_OWNER);
		}
	}
}
