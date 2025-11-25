package org.restapi.springrestapi.service.user;

import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
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
		return UserProfileResult.from(userFinder.findById(id));
	}

	@Override
	public void updateProfile(Long id, PatchProfileRequest request) {
		userValidator.validateDuplicateNickname(request.getNickname());

		User user = userFinder.findById(id);
		user.updateProfile(request);

		userRepository.save(user);
	}

	@Override
	public void changePassword(Long id, ChangePasswordRequest request) {
        authValidator.validateNewPassword(request.password(), request.confirmPassword());

		User user = userFinder.findById(id);
		user.updatePassword(request.password(), passwordEncoder);

		userRepository.save(user);
	}

	@Override
	public void deleteUser(Long id) {
        User user = userFinder.findById(id);
        user.softDelete();
	}
}
