package org.restapi.springrestapi.service.auth;

import org.restapi.springrestapi.dto.auth.LoginResult;
import org.restapi.springrestapi.dto.user.LoginRequest;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;

import org.restapi.springrestapi.validator.AuthValidator;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserFinder userFinder;
    private final AuthValidator authValidator;

	@Override
    @Transactional(readOnly = true)
	public LoginResult login(LoginRequest loginRequest) {
        User user = userFinder.findByEmail(loginRequest.email());

        authValidator.validateSamePassword(loginRequest.password(), user.getPassword());

		return LoginResult.from(user, user.getId().toString());
	}
}
