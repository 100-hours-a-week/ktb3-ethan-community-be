package org.restapi.springrestapi.dto.auth;

import lombok.Builder;
import org.restapi.springrestapi.model.User;


@Builder
public record LoginResult(
	Long userId,
	String nickname,
	String accessToken,
    String profileImageUrl
) {
    public static LoginResult from(User user, String accessToken) {
        return LoginResult.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .accessToken(accessToken)
                .build();
    }
}
