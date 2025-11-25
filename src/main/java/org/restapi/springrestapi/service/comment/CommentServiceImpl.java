package org.restapi.springrestapi.service.comment;

import java.util.List;

import org.restapi.springrestapi.dto.comment.CommentListResult;
import org.restapi.springrestapi.dto.comment.CommentResult;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.dto.comment.RegisterCommentRequest;
import org.restapi.springrestapi.finder.CommentFinder;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.CommentRepository;
import org.restapi.springrestapi.model.Post;
import org.restapi.springrestapi.repository.PostRepository;
import org.restapi.springrestapi.validator.CommentValidator;
import org.restapi.springrestapi.validator.PostValidator;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
	private final CommentRepository commentRepository;
    private final PostRepository postRepository;

	private final CommentFinder commentFinder;
	private final CommentValidator commentValidator;
    private final PostFinder postFinder;
    private final PostValidator postValidator;
    private final UserFinder userFinder;
    private final UserValidator userValidator;

    @Override
	public CommentResult registerComment(Long userId, RegisterCommentRequest request, Long postId) {
        userValidator.validateUserExists(userId);
        postValidator.validatePostExists(postId);

        User user = userFinder.findProxyById(userId);
		Comment comment = Comment.from(request, user);

        Post post = postFinder.findProxyById(postId);
        comment.changePost(post);
        postRepository.increaseCommentCount(postId);

		return CommentResult.from(commentRepository.save(comment));
	}

	@Override
	public CommentListResult getCommentList(Long postId, Long cursor, int limit) {
		postValidator.validatePostExists(postId);

		List<Comment> commentList = commentFinder.findCommentSlice(postId, cursor, limit).getContent();

		if (commentList.isEmpty()) {
			return CommentListResult.empty();
		}

        List<CommentResult> commentResultsList = commentList.stream().map(CommentResult::from).toList();
        final int nextCursor = calcNextCursor(commentList);
		return CommentListResult.from(commentResultsList, nextCursor);
	}

    private int calcNextCursor(List<Comment> commentList) {
        long lastIdDesc = commentList.get(commentList.size() - 1).getId();
        return (int) Math.max(lastIdDesc, 0) + 1;
    }

	@Override
	public CommentResult updateComment(Long userId, PatchCommentRequest request, Long postId, Long id) {
        postValidator.validatePostExists(postId);
        commentValidator.validateOwner(id, userId);

		Comment comment = commentFinder.findById(id);
		comment.updateContent(request.content());

		return CommentResult.from(commentRepository.save(comment));
	}

	@Override
	public void deleteComment(Long userId, Long id, Long postId) {
        userValidator.validateUserExists(userId);
        postValidator.validatePostExists(postId);
        commentValidator.validateCommentExists(id);
        commentValidator.validateOwner(id, userId);

        (commentFinder.findById(id)).changePost(null);

        postRepository.decreaseCommentCount(postId);
		commentRepository.deleteById(id);
	}
}
