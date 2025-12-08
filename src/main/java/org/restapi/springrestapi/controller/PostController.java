package org.restapi.springrestapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.dto.post.PatchPostRequest;
import org.restapi.springrestapi.dto.post.PostListResult;
import org.restapi.springrestapi.dto.post.PostResult;
import org.restapi.springrestapi.dto.post.CreatePostRequest;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.service.post.PostLikeService;
import org.restapi.springrestapi.service.post.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "Posts", description = "게시글 API")
public class PostController {
	private final PostService postService;
    private final PostLikeService postLikeService;


    @Operation(summary = "게시글 등록", description = "새로운 게시글을 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "게시글 등록 성공"),
		@ApiResponse(responseCode = "400", description = "올바르지 않은 형식의 게시글(ex: 게시글 제목 길이 제한, 제목/본문 공백 또는 빈 문자열)"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
	})
	@PostMapping
	public ResponseEntity<APIResponse<PostResult>> createPost(
		@Valid @RequestBody CreatePostRequest req,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
		return ResponseEntity.status(SuccessCode.REGISTER_SUCCESS.getStatus())
			.body(APIResponse.ok(SuccessCode.REGISTER_SUCCESS,
				postService.createPost(principal.user(), req)));
	}


	@Operation(summary = "게시글 목록 조회", description = "커서 기반으로 게시글 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
	})
	@GetMapping
	public ResponseEntity<APIResponse<PostListResult>> getPostList(
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "10") int limit
	) {
		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.GET_SUCCESS,
				postService.getPostList(cursor, limit)));
	}


	@Operation(summary = "게시글 상세 조회", description = "게시글 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
		@ApiResponse(responseCode = "404", description = "조회하려는 게시글이 없음")
	})
	@GetMapping("/{id}")
	public ResponseEntity<APIResponse<PostResult>> getPostDetail(
		@PathVariable Long id,
        HttpServletRequest request,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
		final Long userId = (principal != null) ? principal.getId() : null;

		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.GET_SUCCESS,
				postService.getPost(request, userId, id)));
	}


	@Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "403", description = "게시글 수정 권한 없음"),
		@ApiResponse(responseCode = "404", description = "수정하려는 게시글이 없음")
	})
	@PatchMapping("/{id}")
	public ResponseEntity<APIResponse<Void>> patchPost(
		@PathVariable Long id,
		@Valid @RequestBody PatchPostRequest request,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
        postService.updatePost(principal.getId(), id, request);
        return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.PATCH_SUCCESS));
	}


	@Operation(summary = "게시글 좋아요 토글", description = "로그인 사용자의 좋아요 상태를 토글합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "404", description = "게시글이 존재하지 않아, 좋아요 토글을 할 수 없음")
	})
	@PatchMapping("/{id}/like")
	public ResponseEntity<APIResponse<Void>> updatePostLike(
		@PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
        postLikeService.togglePostLike(principal.getId(), id);
		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.PATCH_SUCCESS));
	}


	@Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "403", description = "게시글 삭제 권한 없음"),
		@ApiResponse(responseCode = "404", description = "삭제하려는 게시글이 없음")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<APIResponse<Void>> deletePost(
		@PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
		postService.deletePost(principal.getId(), id);
		return ResponseEntity.noContent().build();
	}
}
