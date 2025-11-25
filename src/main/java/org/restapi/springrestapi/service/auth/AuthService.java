package org.restapi.springrestapi.service.auth;

import org.restapi.springrestapi.dto.auth.*;

public interface AuthService {
	LoginResult login(LoginRequest loginRequest);
    LoginResult signup(SignUpRequest signUpRequest);
    RefreshTokenResult refresh(Long userId);
}
