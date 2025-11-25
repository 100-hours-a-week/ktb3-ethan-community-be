package org.restapi.springrestapi.dto.user;

public record PatchProfileResponse(
	String nickname,
	String profileImageUrl
) {
	public static PatchProfileResponse from(PatchProfileRequest request) {
		return new PatchProfileResponse(
			request.getNickname(),
			request.getProfileImageUrl()
		);
	}
}
