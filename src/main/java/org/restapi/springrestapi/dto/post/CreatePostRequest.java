package org.restapi.springrestapi.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import org.restapi.springrestapi.common.annotation.ValidPostTitle;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
	@ValidPostTitle
    @NotBlank(message = "게시글 제목은 필수입니다.")
    @Schema(description = "제목", example = "제목입니다.")
	String title,

	@NotBlank(message = "게시글 내용은 필수입니다.")
    @Schema(description = "제목", example = "본문입니다.")
	String content,

	String thumbnailImageUrl
){ }
