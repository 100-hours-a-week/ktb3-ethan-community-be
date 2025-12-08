package org.restapi.springrestapi.exception.code;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements ErrorCode {
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST000","게시글을 찾지 못했습니다."),
	NOT_POST_OWNER(HttpStatus.FORBIDDEN, "POST001", "해당 게시글에 대한 수정/삭제 권한이 없습니다."),
	;

	private final HttpStatus status;
    private final String code;
	private final String message;
}
