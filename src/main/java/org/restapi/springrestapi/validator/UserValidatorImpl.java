package org.restapi.springrestapi.validator;

import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.UserErrorCode;
import org.restapi.springrestapi.finder.UserFinder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidatorImpl implements UserValidator {
    private final UserFinder userFinder;

    @Override
    public void validateUserExists(Long id) {
        if (!userFinder.existsById(id)) {
            throw new AppException(UserErrorCode.USER_NOT_FOUND);
        }
    }

    @Override
    public void validateDuplicateEmail(String email) {
        if (userFinder.existsByEmail(email)) {
            throw new AppException(UserErrorCode.EMAIL_CONFLICT);
        }
    }

    @Override
    public void validateDuplicateNickname(String nickname) {
        if (userFinder.existsByNickName(nickname)) {
            throw new AppException(UserErrorCode.NICKNAME_CONFLICT);
        }
    }
}
