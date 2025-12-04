package org.restapi.springrestapi.service.post;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.RegisterPostRequest;
import org.restapi.springrestapi.dto.post.PostSummary;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.validator.PostValidator;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {
	private final PostRepository postRepository;

	private final PostFinder postFinder;
    private final UserFinder userFinder;
    private final PostValidator postValidator;
    private final UserValidator userValidator;

    private final LocalPostViewDebounce localPostViewDebounce;

    @Override
	public PostSummary createPost(Long userId, RegisterPostRequest command) {
		userValidator.validateUserExists(userId);

        User user = userFinder.findProxyById(userId);
		Post post = Post.from(command);
        post.changeAuthor(user);

		return PostSummary.from(postRepository.save(post));
	}

    @Override
    public PostListResult getPostList(Long cursor, int limit) {
        List<PostSummary> postList = postFinder.findPostSummarySlice(cursor, limit).getContent();
        if (postList.isEmpty() && cursor != null) {
            return PostListResult.from(List.of(), cursor);
        }

        final int nextCursor = calcNextCursor(postList);
        return PostListResult.from(postList, nextCursor);
    }

    @Override
    public PostResult getPost(HttpServletRequest request, Long userIdOrNull, Long id) {
        Post post = postFinder.findById(id);

        final boolean didLike = postFinder.isDidLikeUser(id, userIdOrNull);
        if (!localPostViewDebounce.seenRecently(request, userIdOrNull, id)) {
            postRepository.incrementViewCount(id);
        }

        return PostResult.from(post, didLike);
    }

    private int calcNextCursor(List<PostSummary> postList) {
        long lastIdDesc = postList.get(postList.size() - 1).id();
        return (int) Math.max(lastIdDesc - 1, 1);
    }

	@Override
	public PostResult updatePost(Long userId, Long id, PatchPostRequest command) {
		Post post = postFinder.findById(id);
		postValidator.validateAuthor(userId, id);

		Post saved = Post.from(command, post);

		return PostResult.from(postRepository.save(saved));
	}

	@Override
	public void deletePost(Long userId, Long postId) {
		postValidator.validateAuthor(userId, postId);

        (postFinder.findProxyById(postId)).changeAuthor(null);

		postRepository.deleteById(postId);
	}
}
