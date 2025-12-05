package org.restapi.springrestapi.validator;

import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.exception.code.PostErrorCode;
import org.restapi.springrestapi.finder.PostFinder;
import org.restapi.springrestapi.finder.UserFinder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostValidator {
    private final UserFinder userFinder;
    private final PostFinder postFinder;

    public void validatePostExists(Long postId) {
        if (!postFinder.existsById(postId)) {
            throw new AppException(PostErrorCode.POST_NOT_FOUND);
        }
    }

    public void validateAuthorPermission(Long postId, Long authorId) {
        if (!postFinder.existsByIdAndAuthorId(postId, authorId)) {
            throw new AppException(AuthErrorCode.FORBIDDEN);
        }
    }

    public void validateAuthor(Long authorId, Long postId) {
        userFinder.existsByIdOrThrow(authorId);
        validatePostExists(postId);
        validateAuthorPermission(postId, authorId);
    }
}