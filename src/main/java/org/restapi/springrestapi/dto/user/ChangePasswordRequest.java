package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidPassword;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest (
	@ValidPassword
	String password,

	@NotBlank(message = "password_is_required")
    String confirmPassword
){

}
