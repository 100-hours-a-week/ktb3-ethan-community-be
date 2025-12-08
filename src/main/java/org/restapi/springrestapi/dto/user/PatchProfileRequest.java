package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidNickname;

import io.swagger.v3.oas.annotations.media.Schema;

public record PatchProfileRequest(
	@ValidNickname
	@Schema(description = "새로운 닉네임", example = "newNick")
	String nickname,

	@Schema(description = "새로운 프로필 이미지 url", example = "http://localhost:8080/upload/profile/newProfileImage.jpg")
	String profileImageUrl,

	@Schema(description = "기존 프로필 이미지 삭제 여부", example = "true")
	boolean removeProfileImage
) {  }
