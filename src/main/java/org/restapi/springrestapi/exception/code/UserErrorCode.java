package org.restapi.springrestapi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
	EMAIL_CONFLICT(HttpStatus.CONFLICT, "이미 사용중인 이메일 입니다."),
	NOT_MATCH_NEW_PASSWORD(HttpStatus.BAD_REQUEST, "입력된 두 새로운 비밀번호가 일치하지 않습니다."),
	NICKNAME_CONFLICT(HttpStatus.CONFLICT, "이미 사용중인 닉네임 입니다."),

	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
	;

	private final HttpStatus status;
	private final String message;
}
