package org.restapi.springrestapi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {
	GET_SUCCESS(HttpStatus.OK, "resource_get"),
	PATCH_SUCCESS(HttpStatus.OK, "resource_patch"),
	REGISTER_SUCCESS(HttpStatus.CREATED, "resource_created"),

    AUTH_REQUEST_SUCCESS(HttpStatus.OK, "인증 관련 요청 처리 완료")
	;

	private final HttpStatus status;
	private final String message;
}
