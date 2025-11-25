package org.restapi.springrestapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.dto.post.PatchPostLikeResult;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.RegisterPostRequest;
import org.restapi.springrestapi.dto.post.PostSummary;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.service.post.PostLikeService;
import org.restapi.springrestapi.service.post.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "Posts", description = "게시글 관련 API")
public class PostController {
	private final PostService postService;
    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 등록", description = "현재 로그인 사용자가 새 게시글을 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "등록 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "400", description = "요청 필드 유효성 실패")
	})
	@PostMapping
	public ResponseEntity<APIResponse<PostSummary>> registerPost(
		@Valid @RequestBody RegisterPostRequest request,
        @AuthenticationPrincipal CustomUserDetails user
	) {
		final Long userId = user.getId();

		return ResponseEntity.status(SuccessCode.REGISTER_SUCCESS.getStatus())
			.body(APIResponse.ok(SuccessCode.REGISTER_SUCCESS, postService.registerPost(userId, request)));
	}

	@Operation(summary = "게시글 목록 조회", description = "커서 기반으로 게시글 리스트를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공")
	})
	@GetMapping
	public ResponseEntity<APIResponse<PostListResult>> getPostList(
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "10") int limit
	) {
		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.GET_SUCCESS, postService.getPostList(cursor, limit)));
	}

	@Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "조회 성공"),
		@ApiResponse(responseCode = "404", description = "게시글 없음")
	})
	@GetMapping("/{id}")
	public ResponseEntity<APIResponse<PostResult>> getPostDetail(
		@PathVariable Long id,
        HttpServletRequest request,
        @AuthenticationPrincipal CustomUserDetails user
	) {
		final Long userId = user.getId();

		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.GET_SUCCESS, postService.getPost(request, userId, id)));
	}

	@Operation(summary = "게시글 수정", description = "자신이 작성한 게시글을 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "수정 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "403", description = "작성자 권한 없음"),
		@ApiResponse(responseCode = "404", description = "게시글 없음")
	})
	@PatchMapping("/{id}")
	public ResponseEntity<APIResponse<PostResult>> patchPost(
		@PathVariable Long id,
		@Valid @RequestBody PatchPostRequest request,
        @AuthenticationPrincipal CustomUserDetails user
	) {
		final Long userId = user.getId();
		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.PATCH_SUCCESS, postService.updatePost(userId, id, request)));
	}

	@Operation(summary = "게시글 좋아요 토글", description = "현재 로그인 사용자의 좋아요 상태를 토글합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "토글 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "404", description = "게시글 없음")
	})
	@PatchMapping("/{id}/like")
	public ResponseEntity<APIResponse<PatchPostLikeResult>> updatePostLike(
		@PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails user
	) {
		final Long userId = user.getId();
		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.PATCH_SUCCESS, postLikeService.togglePostLike(userId, id)));
	}

	@Operation(summary = "게시글 삭제", description = "자신이 작성한 게시글을 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "403", description = "작성자 권한 없음"),
		@ApiResponse(responseCode = "404", description = "게시글 없음")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<APIResponse<Void>> deletePost(
		@PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails user
	) {
		final Long userId = user.getId();
		postService.deletePost(userId, id);
		return ResponseEntity.noContent().build();
	}


}
