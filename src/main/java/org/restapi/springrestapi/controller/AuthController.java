package org.restapi.springrestapi.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.dto.auth.*;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.service.auth.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {
	private final AuthService authService;

    @Operation(summary = "로그인", description = "로그인 후 사용자/인증 정보 반환")
	@PostMapping("/login")
	public ResponseEntity<APIResponse<LoginResponse>> login(
		@RequestBody LoginRequest req
	) {
        LoginResult loginResult = authService.login(req);
        return ResponseEntity.status(SuccessCode.GET_SUCCESS.getStatus())
                .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookie().toString())
                .body(APIResponse.ok(SuccessCode.GET_SUCCESS, LoginResponse.from(loginResult)));
	}

    @Operation(summary = "회원가입", description = "회원가입 후 사용자/인증 정보 반환")
    @PostMapping("/signup")
    public ResponseEntity<APIResponse<LoginResponse>> signup(
            @RequestBody SignUpRequest req
    ) {
        LoginResult loginResult = authService.signup(req);
        return ResponseEntity.status(SuccessCode.AUTH_REQUEST_SUCCESS.getStatus())
                .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookie().toString())
                .body(APIResponse.ok(
                        SuccessCode.AUTH_REQUEST_SUCCESS,
                        LoginResponse.from(loginResult))
                );
    }


    @Operation(summary = "엑세스 토큰 재발급", description = "리프레쉬 토큰으로 엑세스 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<TokenResponse>> refresh(HttpServletRequest request) {
        RefreshTokenResult refreshResult = authService.refresh(request);

        return ResponseEntity.status(SuccessCode.AUTH_REQUEST_SUCCESS.getStatus())
                .header(HttpHeaders.SET_COOKIE, refreshResult.refreshCookie().toString())
                .body(APIResponse.ok(
                        SuccessCode.AUTH_REQUEST_SUCCESS,
                        new TokenResponse(refreshResult.accessToken())
                ));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 후 리프레쉬 토큰 삭제")
    @PostMapping("/logout")
    public ResponseEntity<APIResponse<LoginResult>> logout(
            HttpServletResponse res
    ) {
        _deleteRefreshTokenCookie(res);
        return ResponseEntity.noContent().build();
    }

    private void _deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(JwtProvider.REFRESH_COOKIE, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}