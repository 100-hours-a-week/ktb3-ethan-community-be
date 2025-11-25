package org.restapi.springrestapi.dto.auth;

import org.springframework.http.ResponseCookie;

public record RefreshTokenResult(
        String accessToken,
        ResponseCookie refreshCookie
) { }
