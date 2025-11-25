package org.restapi.springrestapi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
	INVALID_EMAIL_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"사용자 인증이 필요합니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다. 다시 로그인 해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다.")
	;

	private final HttpStatus status;
	private final String message;
}
