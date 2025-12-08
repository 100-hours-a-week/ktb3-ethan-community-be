package org.restapi.springrestapi.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements ErrorCode {
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT000","댓글을 찾지 못했습니다."),
	NOT_COMMENT_OWNER(HttpStatus.FORBIDDEN, "COMMENT001", "해당 댓글에 대한 수정/삭제 권한이 없습니다."),
	COMMENT_NOT_BELONG_TO_POST(HttpStatus.BAD_REQUEST, "COMMENT003", "해당 댓글은 요청하신 게시글에 속하지 않습니다.")
	;


	private final HttpStatus status;
    private final String code;
	private final String message;
}
