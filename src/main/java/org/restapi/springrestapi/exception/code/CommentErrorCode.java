package org.restapi.springrestapi.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements ErrorCode {
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT000","댓글을 찾지 못했습니다."),
	;

	private final HttpStatus status;
    private final String code;
	private final String message;
}
