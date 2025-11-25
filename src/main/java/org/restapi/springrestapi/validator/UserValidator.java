package org.restapi.springrestapi.validator;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserValidator {
    void validateUserExists(Long id);
    void validateDuplicateEmail(String email);
    void validateDuplicateNickname(String nickname);
    default void validateRegisterUser(String email, String nickname) {
        validateDuplicateEmail(email);
        validateDuplicateNickname(nickname);
    }
}
