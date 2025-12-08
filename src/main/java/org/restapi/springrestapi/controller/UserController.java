package org.restapi.springrestapi.controller;

import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.dto.user.UserProfileResult;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "사용자 API")
public class UserController {
	private final UserService userService;


	@Operation(summary = "사용자 정보 조회", description = "사용자 ID로 프로필을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 사용자.")
	})
	@GetMapping("/{id}")
	public ResponseEntity<APIResponse<UserProfileResult>> getUserProfile(
		@PathVariable Long id
	) {
		return ResponseEntity.ok()
                .body(APIResponse.ok(SuccessCode.GET_SUCCESS,
					userService.getUserProfile(id)));
	}


	@Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
		@ApiResponse(responseCode = "400", description = "올바르지 않은 형식의 닉네임"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "409", description = "중복된 닉네임 사용")
	})
	@PatchMapping
	public ResponseEntity<APIResponse<Void>> updateProfile(
		@Valid @RequestBody PatchProfileRequest request,
		@AuthenticationPrincipal CustomUserDetails user
	) {
		userService.updateProfile(user.getId(), request);
		return ResponseEntity.ok(APIResponse.ok(SuccessCode.PATCH_SUCCESS));
	}


	@Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "비밀번호 변경 성공"),
		@ApiResponse(responseCode = "400", description = "새 비밀번호와 확인용 새 비밀번호 불일치"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "409", description = "기존 비밀번호와 동일한 비밀번호"),
	})
	@PutMapping("/password")
	public ResponseEntity<Void> changePassword(
		@Valid @RequestBody ChangePasswordRequest request,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
		userService.updatePassword(principal.user(), request);
		return ResponseEntity.noContent().build();
	}


	@Operation(summary = "회원 탈퇴", description = "현재 로그인 사용자를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "로그인 필요")
	})
	@DeleteMapping
	public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
		userService.deleteUser(user.getId());
		return ResponseEntity.noContent().build();
	}
}
