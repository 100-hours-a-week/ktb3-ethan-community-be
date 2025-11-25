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

	LOGIN_SUCCESS(HttpStatus.OK, "login_success")
	;

	private final HttpStatus status;
	private final String message;
}
