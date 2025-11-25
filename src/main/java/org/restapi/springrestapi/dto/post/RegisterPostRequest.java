package org.restapi.springrestapi.dto.post;

import org.restapi.springrestapi.common.annotation.ValidPostTitle;

import jakarta.validation.constraints.NotBlank;

public record RegisterPostRequest (
	@ValidPostTitle
	String title,

	@NotBlank
	String content,

	String thumbnailImageUrl // nullable
){

}
