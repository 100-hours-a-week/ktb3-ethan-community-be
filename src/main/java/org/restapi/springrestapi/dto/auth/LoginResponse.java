package org.restapi.springrestapi.dto.auth;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl,
        String accessToken
) {
    public static LoginResponse from(LoginResult loginResult) {
        return new LoginResponse(loginResult.id(), loginResult.email(), loginResult.nickname(), loginResult.profileImageUrl(), loginResult.accessToken());
    }
}
