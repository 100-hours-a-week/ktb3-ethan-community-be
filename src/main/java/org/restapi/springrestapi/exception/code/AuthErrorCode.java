package org.restapi.springrestapi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {
	INVALID_EMAIL_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH000", "이메일 또는 비밀번호가 올바르지 않습니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"AUTH001", "사용자 인증이 필요합니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH002", "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH003", "리프레시 토큰이 유효하지 않습니다. 다시 로그인 해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH004", "권한이 없습니다."),
    COOKIE_MISSING(HttpStatus.BAD_REQUEST, "AUTH005", "쿠키가 없습니다."),
    REFRESH_COOKIE_MISSING(HttpStatus.UNAUTHORIZED, "AUTH006", "리프레시 쿠키가 없습니다."),
	FORBIDDEN_COMMENT(HttpStatus.FORBIDDEN, "AUTH007", "댓글 수정 권한이 없습니다."),
	NOT_MATCH_NEW_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH008","입력된 두 비밀번호가 일치하지 않습니다."),
	PASSWORD_DUPLICATED(HttpStatus.CONFLICT, "AUTH009", "새로운 비밀번호가 이전 비밀번호와 동일합니다."),
	;

	private final HttpStatus status;
    private final String code;
	private final String message;
}
