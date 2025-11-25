package org.restapi.springrestapi.validator;

import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.exception.code.PostErrorCode;
import org.restapi.springrestapi.finder.PostFinder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostValidatorImpl implements PostValidator {
    private final UserValidator userValidator;
    private final PostFinder postFinder;

    @Override
    public void validatePostExists(Long postId) {
        if (!postFinder.existsById(postId)) {
            throw new AppException(PostErrorCode.POST_NOT_FOUND);
        }
    }

    @Override
    public void validateAuthorPermission(Long postId, Long authorId) {
        if (!postFinder.existsByIdAndAuthorId(postId, authorId)) {
            throw new AppException(AuthErrorCode.FORBIDDEN);
        }
    }

    @Override
    public void validateAuthor(Long authorId, Long postId) {
        userValidator.validateUserExists(authorId);
        validatePostExists(postId);
        validateAuthorPermission(postId, authorId);
    }
}