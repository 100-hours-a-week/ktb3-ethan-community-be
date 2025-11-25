package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidNickname;

import lombok.Getter;

@Getter
public class PatchProfileRequest {
	@ValidNickname
	private String nickname;

	private String profileImageUrl;
}
