package org.restapi.springrestapi.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.dto.auth.*;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {
	private final AuthService authService;

    @Operation(summary = "로그인", description = "로그인 후 사용자/인증 정보 반환")
	@PostMapping("/login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "필수 인자 누락 또는 형식 불일치"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
	public ResponseEntity<APIResponse<LoginResponse>> login(
		@Valid @RequestBody LoginRequest req
	) {
        LoginResult loginResult = authService.login(req);
        return ResponseEntity.status(SuccessCode.GET_SUCCESS.getStatus())
                .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookie().toString())
                .body(APIResponse.ok(SuccessCode.GET_SUCCESS, LoginResponse.from(loginResult)));
	}

    @Operation(summary = "회원가입", description = "회원가입 후 사용자/인증 정보 반환")
    @PostMapping("/signup")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "필수 인자 누락 또는 형식 불일치"),
            @ApiResponse(responseCode = "409", description = "중복된 이메일 또는 닉네임")
    })
    public ResponseEntity<APIResponse<LoginResponse>> signup(
            @Valid @RequestBody SignUpRequest req
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
        deleteRefreshTokenCookie(res);
        return ResponseEntity.noContent().build();
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(JwtProvider.REFRESH_COOKIE, null);
        cookie.setPath("/auth/refresh");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}