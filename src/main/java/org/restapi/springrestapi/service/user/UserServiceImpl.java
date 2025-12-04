package org.restapi.springrestapi.service.user;

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
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final UserFinder userFinder;
	private final PasswordEncoder passwordEncoder;
	private final UserValidator userValidator;
    private final AuthValidator authValidator;

	@Override
	public UserProfileResult getUserProfile(Long id) {
		return UserProfileResult.from(userFinder.findByIdOrThrow(id));
	}

	@Override
	public void updateProfile(Long id, PatchProfileRequest request) {
		userValidator.validateDuplicateNickname(request.nickname());

		User user = userFinder.findByIdOrThrow(id);
		user.updateProfile(request);

		userRepository.save(user);
	}

	@Override
	public void updatePasswod(Long id, ChangePasswordRequest request) {
        authValidator.validateNewPassword(request.password(), request.confirmPassword());
		User user = userFinder.findByIdOrThrow(id);

		user.updatePassword(new EncodedPassword(passwordEncoder.encode(request.password())));

		userRepository.save(user);
	}

	@Override
	public void deleteUser(Long id) {
        userRepository.deleteById(id);
	}
}
