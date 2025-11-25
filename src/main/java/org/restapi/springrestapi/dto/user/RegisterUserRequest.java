package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidEmail;
import org.restapi.springrestapi.common.annotation.ValidNickname;
import org.restapi.springrestapi.common.annotation.ValidPassword;

import lombok.Getter;

@Getter
public class RegisterUserRequest {
	@ValidEmail
	private String email;

	@ValidPassword
	private String password;

	@ValidNickname
	private String nickname;

	private String profileImageUrl;
}
