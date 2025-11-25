package org.restapi.springrestapi.exception;

import lombok.Getter;
import org.restapi.springrestapi.exception.code.ErrorCode;

import javax.naming.AuthenticationException;

@Getter
public class AuthException extends AuthenticationException {
    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
