package org.restapi.springrestapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.restapi.springrestapi.common.annotation.ValidPassword;

public record LoginRequest(
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 입력값입니다.")
	@Schema(description = "이메일", example = "test001@test.com")
	String email,

	@ValidPassword
    @Schema(description = "비밀번호", example = "testTT1!")
	String password
) { }
