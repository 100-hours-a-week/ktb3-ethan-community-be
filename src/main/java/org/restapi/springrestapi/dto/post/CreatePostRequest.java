package org.restapi.springrestapi.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import org.restapi.springrestapi.common.annotation.ValidPostTitle;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
	@ValidPostTitle
    @Schema(description = "게시글 제목", example = "오늘도 좋은 하루!")
	String title,

	@NotBlank(message = "게시글 내용은 필수입니다.")
    @Schema(description = "게시글 본문", example = "오늘도 좋은 하루입니다. 여러분들의 조각을 공유해주세요!")
	String content,

	@Schema(description = "게시글 대표 이미지 url", example = "http://localhost:8080/upload/post/thumbnailImage.jpg")
	String thumbnailImageUrl
){ }
