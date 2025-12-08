package org.restapi.springrestapi.validator;

import lombok.RequiredArgsConstructor;

import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthValidator {
    private final PasswordEncoder passwordEncoder;

    public void validateNewPassword(
		ChangePasswordRequest req,
		String oldEncodePassword
	) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new AppException(AuthErrorCode.NOT_MATCH_NEW_PASSWORD);
        }

		if (!passwordEncoder.matches(req.password(), oldEncodePassword)) {
			throw new AppException(AuthErrorCode.PASSWORD_DUPLICATED);
		}
    }
}
