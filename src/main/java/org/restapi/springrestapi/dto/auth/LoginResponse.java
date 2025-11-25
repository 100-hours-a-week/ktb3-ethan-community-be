package org.restapi.springrestapi.dto.auth;

public record LoginResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String accessToken
) {
    public static LoginResponse from(LoginResult loginResult) {
        return new LoginResponse(loginResult.userId(), loginResult.nickname(), loginResult.profileImageUrl(), loginResult.accessToken());
    }
}
