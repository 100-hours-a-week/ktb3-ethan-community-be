package org.restapi.springrestapi.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.restapi.springrestapi.dto.auth.*;
import org.restapi.springrestapi.dto.user.EncodedPassword;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;

import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.security.CustomUserDetails;
import org.restapi.springrestapi.security.jwt.JwtProvider;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;
    private final UserRepository userRepository;
    private final UserFinder userFinder;

    @Override
    @Transactional(readOnly = true)
	public LoginResult login(LoginRequest loginRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );

            CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
            User user = principal.user();

            String access = jwtProvider.createAccessToken(user.getId());
            String refresh = jwtProvider.createRefreshToken(user.getId());

            ResponseCookie refreshCookie = jwtProvider.createRefreshCookie(refresh);

            return LoginResult.from(user, access, refreshCookie);
        } catch (AuthenticationException ex) {
            throw new AppException(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }
    }

    @Override
    public LoginResult signup(SignUpRequest signUpRequest) {
        userValidator.validateSignUpUser(signUpRequest.email(), signUpRequest.nickname());

        User user = User.from(signUpRequest, new EncodedPassword(passwordEncoder.encode(signUpRequest.password())));

        User saved = userRepository.save(user);

        String accessToken = jwtProvider.createAccessToken(saved.getId());
        String refresh = jwtProvider.createRefreshToken(saved.getId());

        ResponseCookie refreshCookie = jwtProvider.createRefreshCookie(refresh);

        return LoginResult.from(saved, accessToken, refreshCookie);
    }


    @Override
    public RefreshTokenResult refresh(HttpServletRequest request) {
        String refreshToken = jwtProvider.resolveRefreshToken(request).get();
        Long userId = jwtProvider.getUserIdFromRefresh(refreshToken);

        userFinder.existsByIdOrThrow(userId);

        String newAccess = jwtProvider.createAccessToken(userId);

        String newRefresh = jwtProvider.createRefreshToken(userId);
        ResponseCookie newRefreshCookie = jwtProvider.createRefreshCookie(newRefresh);

        return new RefreshTokenResult(newAccess, newRefreshCookie);
    }
}
