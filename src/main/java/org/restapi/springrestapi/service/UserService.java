package org.restapi.springrestapi.service;

import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.dto.user.EncodedPassword;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.dto.user.UserProfileResult;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.validator.AuthValidator;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
	private final UserRepository userRepository;
	private final UserFinder userFinder;
	private final PasswordEncoder passwordEncoder;
	private final UserValidator userValidator;
    private final AuthValidator authValidator;

	public UserProfileResult getUserProfile(Long id) {
		return UserProfileResult.from(userFinder.findByIdOrThrow(id));
	}

	public void updateProfile(Long id, PatchProfileRequest req) {
		userValidator.validateDuplicateNickname(req.nickname());

		User user = userFinder.findByIdOrThrow(id);
		user.updateProfile(req);

		userRepository.save(user);
	}

	public void updatePassword(User user, ChangePasswordRequest req) {
        authValidator.validateNewPassword(req, user.getPassword());
		user.updatePassword(new EncodedPassword(passwordEncoder.encode(req.password())));

		userRepository.save(user);
	}

	public void deleteUser(Long id) {
        userRepository.deleteById(id);
	}
}
