package org.restapi.springrestapi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode {
	GET_SUCCESS(HttpStatus.OK, "SUCCESS000", "resource_get"),
	PATCH_SUCCESS(HttpStatus.OK, "SUCCESS001","resource_patch"),
	REGISTER_SUCCESS(HttpStatus.CREATED, "SUCCESS002","resource_created"),

    AUTH_REQUEST_SUCCESS(HttpStatus.OK, "SUCCESS003", "인증 관련 요청 처리 완료")
	;

	private final HttpStatus status;
    private final String code;
	private final String message;
}
