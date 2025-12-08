package org.restapi.springrestapi.service;

import java.util.List;

import org.restapi.springrestapi.dto.comment.CommentListResult;
import org.restapi.springrestapi.dto.comment.CommentResult;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.CommentErrorCode;
import org.restapi.springrestapi.finder.CommentFinder;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.Comment;
import org.restapi.springrestapi.repository.CommentRepository;
import org.restapi.springrestapi.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
	private final CommentRepository commentRepository;
    private final PostRepository postRepository;

	private final PostFinder postFinder;
	private final UserFinder userFinder;
	private final CommentFinder commentFinder;

	public CommentResult createComment(Long userId, CreateCommentRequest request, Long postId) {
		postFinder.existsByIdOrThrow(postId);

		Comment comment = Comment.from(request,
			userFinder.findProxyById(userId),
			postFinder.findProxyById(postId)
		);

        postRepository.increaseCommentCount(postId);

		return CommentResult.from(commentRepository.save(comment));
	}

	public CommentListResult getCommentList(Long postId, Long cursor, int limit) {
		postFinder.existsByIdOrThrow(postId);

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

	public CommentResult updateComment(Long userId, PatchCommentRequest request, Long postId, Long id) {
		Comment comment = commentFinder.findByIdOrThrow(id);
		validatePermission(comment, userId, postId);

		comment.updateContent(request);

		return CommentResult.from(comment);
	}

	public void deleteComment(Long userId, Long postId, Long id) {
		Comment comment = commentFinder.findByIdOrThrow(id);
		validatePermission(comment, userId, postId);

        postRepository.decreaseCommentCount(postId);
		commentRepository.deleteById(id);
	}

	private void validatePermission(Comment comment, Long authorId, Long postId) {
		if (!comment.getUser().getId().equals(authorId)) {
			throw new AppException(CommentErrorCode.NOT_COMMENT_OWNER);
		}

		if (!comment.getPost().getId().equals(postId)) {
			throw new AppException(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST);
		}
	}
}
