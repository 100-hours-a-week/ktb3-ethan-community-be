package org.restapi.springrestapi.dto.post;

import org.restapi.springrestapi.common.annotation.ValidPostTitle;

import jakarta.validation.constraints.NotBlank;

public record PatchPostRequest(
	@ValidPostTitle
	String title,

	String content,

	String thumbnailImageUrl,

    boolean removeThumbnailImage
) { }
