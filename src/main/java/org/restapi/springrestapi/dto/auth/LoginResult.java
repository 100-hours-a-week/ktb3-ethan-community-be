package org.restapi.springrestapi.dto.auth;

import lombok.Builder;
import org.restapi.springrestapi.model.User;
import org.springframework.http.ResponseCookie;


@Builder
public record LoginResult(
    Long id,
    String email,
	String nickname,
    String profileImageUrl,
	String accessToken,
    ResponseCookie refreshCookie
) {
    public static LoginResult from(User user, String accessToken, ResponseCookie refreshCookie) {
        return LoginResult.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .accessToken(accessToken)
                .refreshCookie(refreshCookie)
                .build();
    }
}
