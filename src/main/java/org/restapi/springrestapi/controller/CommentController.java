package org.restapi.springrestapi.controller;

import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.dto.comment.CommentListResult;
import org.restapi.springrestapi.dto.comment.CommentResult;
import org.restapi.springrestapi.dto.comment.PatchCommentRequest;
import org.restapi.springrestapi.dto.comment.CreateCommentRequest;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.service.CommentService;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Comments", description = "댓글 API")
public class CommentController {
	private final CommentService commentService;


	@Operation(summary = "댓글 등록", description = "게시글에 새로운 댓글을 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "댓글 작성 성공"),
		@ApiResponse(responseCode = "400", description = "올바르지 않은 형식의 댓글(ex: 공백 또는 빈 문자열)"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "404", description = "댓글을 작성하려는 게시글이 존재하지 않음"),
	})
	@PostMapping("/{postId}/comments")
	public ResponseEntity<APIResponse<CommentResult>> createComment(
		@PathVariable Long postId,
		@Valid @RequestBody CreateCommentRequest request,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(APIResponse.ok(SuccessCode.REGISTER_SUCCESS, commentService.createComment(principal.getId(), request, postId)));
	}


	@Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "댓글을 조회하려는 게시글이 존재하지 않음"),
	})
	@GetMapping("/{postId}/comments")
	public ResponseEntity<APIResponse<CommentListResult>> getCommentAll(
		@PathVariable Long postId,
		@RequestParam(required = false) Long cursor,
		@RequestParam(defaultValue = "10") int limit
	) {
		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.GET_SUCCESS,
				commentService.getCommentList(postId, cursor, limit)));
	}


	@Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
		@ApiResponse(responseCode = "400", description = "올바르지 않은 형식의 댓글(ex: 공백 또는 빈 문자열)"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "404", description = "수정하려는 댓글 또는, 댓글이 속한 게시글이 없음")
	})
	@PatchMapping("/{postId}/comments/{id}")
	public ResponseEntity<APIResponse<CommentResult>> patchComment(
		@PathVariable Long postId,
		@PathVariable Long id,
		@Valid @RequestBody PatchCommentRequest request,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
		return ResponseEntity.ok()
			.body(APIResponse.ok(SuccessCode.PATCH_SUCCESS,
				commentService.updateComment(principal.getId(), request, postId, id)));
	}


	@Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "삭제 성공"),
		@ApiResponse(responseCode = "401", description = "로그인 필요"),
		@ApiResponse(responseCode = "404", description = "수정하려는 댓글 또는, 댓글이 속한 게시글이 없음")
	})
	@DeleteMapping("/{postId}/comments/{id}")
	public ResponseEntity<Void> deleteComment(
		@PathVariable Long postId,
		@PathVariable Long id,
        @AuthenticationPrincipal CustomUserDetails principal
	) {
		commentService.deleteComment(principal.getId(), postId, id);
		return ResponseEntity.noContent().build();
	}
}
