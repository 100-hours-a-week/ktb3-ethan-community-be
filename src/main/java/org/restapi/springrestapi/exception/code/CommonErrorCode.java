package org.restapi.springrestapi.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
	NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON000","찾을 수 없는 자원"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON001","올바르지 않은 요청: %s"),
	INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON002","API 서버 에러");

	private final HttpStatus status;
    private final String code;
	private final String message;
}
