package org.restapi.springrestapi.service.comment;

import org.restapi.springrestapi.dto.comment.CommentListResult;
import org.restapi.springrestapi.dto.comment.CommentResult;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;

public interface CommentService {
	CommentResult createComment(Long userId, CreateCommentRequest request, Long postId);
	CommentListResult getCommentList(Long postId, Long cursor, int limit);
	CommentResult updateComment(Long userId, PatchCommentRequest request, Long postId, Long id);
	void deleteComment(Long userId, Long postId, Long id);
}
