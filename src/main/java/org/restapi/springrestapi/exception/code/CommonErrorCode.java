package org.restapi.springrestapi.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "invalid_request"),
	NOT_FOUND(HttpStatus.NOT_FOUND, "not_found"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "bad_request: %s"),
	INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");

	private final HttpStatus status;
	private final String message;
}
