package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest (
	@ValidPassword
	@Schema(description = "새로운 비밀번호", example = "testTT1!!")
	String password,

	@NotBlank(message = "새로운 비밀번호 확인 입력 누락")
	@Schema(description = "새로운 비밀번호 확인", example = "testTT1!!")
    String confirmPassword
){

}
