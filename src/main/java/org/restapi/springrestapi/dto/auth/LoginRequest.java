package org.restapi.springrestapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import org.restapi.springrestapi.common.annotation.ValidEmail;
import org.restapi.springrestapi.common.annotation.ValidPassword;

public record LoginRequest(
	@ValidEmail
    @Schema(description = "이메일", example = "test001@test.com")
	String email,

	@ValidPassword
    @Schema(description = "비밀번호", example = "testTT1!")
	String password
) {

}
