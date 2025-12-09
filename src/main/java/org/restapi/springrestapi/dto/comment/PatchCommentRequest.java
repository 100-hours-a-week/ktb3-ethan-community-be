package org.restapi.springrestapi.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PatchCommentRequest(
	@NotBlank
	@Schema(description = "수정할 댓글 내용", example = "좋은 사진이네요~?")
	String content
) { }
