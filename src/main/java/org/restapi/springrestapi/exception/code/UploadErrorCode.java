package org.restapi.springrestapi.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UploadErrorCode implements ErrorCode {
	FILE_SIZE_OVER(HttpStatus.BAD_REQUEST, "FILE000","파일 최대 크기 초과"),
	INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE001","지원되지 않는 파일 타입");

	private final HttpStatus status;
    private final String code;
	private final String message;
}
