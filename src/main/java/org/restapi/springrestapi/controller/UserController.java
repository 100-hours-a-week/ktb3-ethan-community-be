package org.restapi.springrestapi.controller;

import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.dto.user.PatchProfileResponse;
import org.restapi.springrestapi.dto.user.RegisterUserRequest;
import org.restapi.springrestapi.dto.user.RegisterUserResponse;
import org.restapi.springrestapi.dto.user.UserProfileResult;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.AuthContext;
import org.restapi.springrestapi.service.user.UserService;
import org.restapi.springrestapi.common.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "사용자 관련 API")
public class UserController {
	private final UserService userService;
	private final AuthContext authContext;

	@Operation(summary = "회원 가입", description = "신규 사용자를 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "회원 가입 성공"),
		@ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복"),
		@ApiResponse(responseCode = "400", description = "요청 필드 유효성 실패")
	})
	@PostMapping
	public ResponseEntity<APIResponse<RegisterUserResponse>> register(
		@Valid @RequestBody RegisterUserRequest request
	) {
		final Long id = userService.register(request);
		return ResponseEntity.status(SuccessCode.REGISTER_SUCCESS.getStatus())
				.body(APIResponse.ok(SuccessCode.REGISTER_SUCCESS, new RegisterUserResponse(id)));
	}
	@Operation(summary = "사용자 프로필 조회", description = "사용자 ID로 프로필을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "사용자 없음")
	})
	@GetMapping("/{id}")
	public ResponseEntity<APIResponse<UserProfileResult>> getUserProfile(
		@PathVariable Long id
	) {
		return ResponseEntity.ok()
				.body(APIResponse.ok(SuccessCode.GET_SUCCESS, userService.getUserProfile(id)));
	}

	@Operation(summary = "프로필 수정", description = "현재 로그인 사용자의 프로필 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "409", description = "닉네임 중복"),
		@ApiResponse(responseCode = "400", description = "요청 필드 유효성 실패")
	})
	@PatchMapping
	public ResponseEntity<APIResponse<PatchProfileResponse>> updateProfile(
		@Valid @RequestBody PatchProfileRequest request
	) {
		final Long id = authContext.requiredUserId();
		userService.updateProfile(id, request);
		return ResponseEntity.ok(APIResponse.ok(
			SuccessCode.PATCH_SUCCESS, PatchProfileResponse.from(request)));
	}

	@Operation(summary = "비밀번호 변경", description = "현재 로그인 사용자의 비밀번호를 변경합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "변경 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "400", description = "새 비밀번호와 확인 비밀번호 불일치")
	})
	@PatchMapping("/password")
	public ResponseEntity<APIResponse<Void>> changePassword(
		@Valid @RequestBody ChangePasswordRequest request
	) {
		final Long id = authContext.requiredUserId();
		userService.changePassword(id, request);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Operation(summary = "회원 탈퇴", description = "현재 로그인 사용자를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@DeleteMapping
	public ResponseEntity<Void> deleteUser() {
		final Long id = authContext.requiredUserId();
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}
}
