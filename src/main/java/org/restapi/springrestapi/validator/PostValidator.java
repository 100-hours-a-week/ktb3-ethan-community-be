package org.restapi.springrestapi.validator;

public interface PostValidator {
    void validatePostExists(Long postId);
    void validateAuthorPermission(Long postId, Long authorId);
    void validateAuthor(Long userId, Long postId);
}