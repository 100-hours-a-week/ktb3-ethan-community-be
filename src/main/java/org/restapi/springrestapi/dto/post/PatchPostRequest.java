package org.restapi.springrestapi.dto.post;

import org.restapi.springrestapi.common.annotation.ValidPostTitle;

import io.swagger.v3.oas.annotations.media.Schema;

public record PatchPostRequest(
	@ValidPostTitle
	@Schema(description = "수정할 게시글 제목", example = "오늘도 좋은 하루?")
	String title,

	@Schema(description = "수정할 게시글 본문", example = "오늘도 좋은 하루입니다? 여러분들의 조각을 공유해주세요?")
	String content,

	@Schema(description = "게시글 대표 이미지 url", example = "http://localhost:8080/upload/post/newThumbnailImage.jpg")
	String thumbnailImageUrl,

	@Schema(description = "기존 게시글 대표 이미지 삭제 여부", example = "true")
    boolean removeThumbnailImage
) { }
