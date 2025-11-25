package org.restapi.springrestapi.validator;

public interface CommentValidator {
    void validateCommentExists(Long commendId);
    void validateOwner(Long commentId, Long userId);
}