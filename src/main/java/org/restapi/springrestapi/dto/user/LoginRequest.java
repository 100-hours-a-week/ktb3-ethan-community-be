package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidEmail;
import org.restapi.springrestapi.common.annotation.ValidPassword;

public record LoginRequest(
	@ValidEmail
	String email,
	@ValidPassword
	String password
) {

}
