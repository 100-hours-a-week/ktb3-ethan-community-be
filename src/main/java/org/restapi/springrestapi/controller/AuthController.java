package org.restapi.springrestapi.controller;

import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.dto.auth.LoginResult;
import org.restapi.springrestapi.dto.user.LoginRequest;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {
	private final AuthService authService;

	@Operation(summary = "사용자 로그인(토큰 발급)", description = "사용자 로그인을 처리하고, 토큰(userId)을 발급합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "로그인\n 성공"),
		@ApiResponse(responseCode = "400", description = "email 또는 password 누락"),
		@ApiResponse(responseCode = "401", description = "잘못된 email 또는 password 형식")
	})
	@PostMapping("/token")
	public ResponseEntity<APIResponse<LoginResult>> login(
		@RequestBody LoginRequest request
	) {
		return ResponseEntity.ok(APIResponse.ok(SuccessCode.LOGIN_SUCCESS, authService.login(request)));
	}

}