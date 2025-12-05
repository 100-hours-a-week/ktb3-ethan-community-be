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
public class UserValidator {
    private final UserFinder userFinder;


    public void validateUserExists(Long id) {
    }

    public void validateDuplicateEmail(String email) {
        if (userFinder.existsByEmail(email)) {
            throw new AppException(UserErrorCode.EMAIL_CONFLICT);
        }
    }

    public void validateDuplicateNickname(String nickname) {
        if (userFinder.existsByNickName(nickname)) {
            throw new AppException(UserErrorCode.NICKNAME_CONFLICT);
        }
    }
    public void validateSignUpUser(String email, String nickname) {
        validateDuplicateEmail(email);
        validateDuplicateNickname(nickname);
    }
}
