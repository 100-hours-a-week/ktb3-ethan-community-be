package org.restapi.springrestapi.service.auth;

import org.restapi.springrestapi.dto.auth.LoginResult;
import org.restapi.springrestapi.dto.user.LoginRequest;

public interface AuthService {
	LoginResult login(LoginRequest loginRequest);
}
