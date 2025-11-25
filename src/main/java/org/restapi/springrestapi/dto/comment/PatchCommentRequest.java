package org.restapi.springrestapi.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record PatchCommentRequest(
	@NotBlank
	String content
) {

}
