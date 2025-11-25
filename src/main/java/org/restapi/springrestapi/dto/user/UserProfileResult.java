package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.model.User;

import lombok.Builder;

@Builder
public record UserProfileResult(
	Long id,
    String email,
	String nickname,
	String profileImageUrl
) {
	public static UserProfileResult from(User userProfileResult) {
		return UserProfileResult.builder()
			.id(userProfileResult.getId())
			.email(userProfileResult.getEmail())
			.nickname(userProfileResult.getNickname())
			.profileImageUrl(userProfileResult.getProfileImageUrl())
			.build();
	}
}
