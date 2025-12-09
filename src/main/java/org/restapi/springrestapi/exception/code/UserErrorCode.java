package org.restapi.springrestapi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
	EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER000", "이미 사용중인 이메일 입니다."),
	NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER001","이미 사용중인 닉네임 입니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER002", "사용자를 찾을 수 없습니다.")
	;

	private final HttpStatus status;
    private final String code;
	private final String message;
}
