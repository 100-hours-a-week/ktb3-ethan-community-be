package org.restapi.springrestapi.controller;

import org.restapi.springrestapi.dto.upload.UploadImageResponse;
import org.restapi.springrestapi.exception.code.SuccessCode;
import org.restapi.springrestapi.common.APIResponse;
import org.restapi.springrestapi.common.util.FileStorageService;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Upload", description = "파일 업로드 API")
public class UploadController {
	private final FileStorageService fileStorageService;

	@Operation(summary = "사용자 프로필 이미지 업로드 api", description = "사용자의 이미지를 업로드하고 URL을 반환합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "업로드 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 요청")
	})
    @PostMapping(
            value = "/upload/profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<APIResponse<UploadImageResponse>> uploadProfileImage(
            @RequestPart("image") MultipartFile image
    ) {
        String url = fileStorageService.saveProfileImage(image);

        return ResponseEntity.status(201)
                .body(APIResponse.ok(SuccessCode.REGISTER_SUCCESS, new UploadImageResponse(url)));
    }

    @Operation(summary = "게시글 대표 이미지 업로드 api", description = "게시글 대표 이미지를 업로드하고 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 요청")
    })
    @PostMapping(
            value = "/upload/post",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<APIResponse<UploadImageResponse>> uploadPostImage(
            @RequestPart("image") MultipartFile image
    ) {
        String url = fileStorageService.savePostImage(image);

        return ResponseEntity.status(201)
                .body(APIResponse.ok(SuccessCode.REGISTER_SUCCESS, new UploadImageResponse(url)));
    }
}
