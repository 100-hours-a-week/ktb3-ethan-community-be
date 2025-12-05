package org.restapi.springrestapi.service.post;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.dto.post.PostSummary;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.repository.UserRepository;
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
    private final UserRepository userRepository;

	private final PostFinder postFinder;
    private final PostValidator postValidator;
    private final UserFinder userFinder;

    private final LocalPostViewDebounce localPostViewDebounce;

    @Override
	public PostSummary createPost(Long userId, CreatePostRequest command) {
        userFinder.existsByIdOrThrow(userId);

        User user = userRepository.getReferenceById(userId);
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
        Post post = postFinder.findByIdOrThrow(id);

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
	public void patchPost(Long userId, Long id, PatchPostRequest command) {
		Post post = postFinder.findByIdOrThrow(id);
		postValidator.validateAuthor(userId, id);

        post.patch(command);
	}

	@Override
	public void deletePost(Long userId, Long postId) {
		postValidator.validateAuthor(userId, postId);

        (postFinder.findProxyById(postId)).changeAuthor(null);

		postRepository.deleteById(postId);
	}
}
