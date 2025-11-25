package org.restapi.springrestapi.validator;

import lombok.RequiredArgsConstructor;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.exception.code.UserErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthValidatorImpl implements AuthValidator {
    private final PasswordEncoder passwordEncoder;

    @Override
    public void validateSamePassword(String loginPassword, String userPassword) {
        if (!passwordEncoder.matches(loginPassword, userPassword)) {
            throw new AppException(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }
    }

    @Override
    public void validateNewPassword(String newPassword, String newConfirmPassword) {
        if (!newPassword.equals(newConfirmPassword)) {
            throw new AppException(UserErrorCode.NOT_MATCH_NEW_PASSWORD);
        }
    }
}
