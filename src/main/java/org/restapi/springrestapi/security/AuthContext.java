package org.restapi.springrestapi.security;

import java.util.Optional;

import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;

public interface AuthContext {
	Long currentUserIdOrNull();
	default Long requiredUserId() {
//		return Optional.ofNullable(currentUserIdOrNull())
//			.orElseThrow(() -> new AppException(AuthErrorCode.AUTH_REQUIRED));
        return null;
	}
}
