package org.restapi.springrestapi.finder;

import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.UserErrorCode;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFinderImpl implements UserFinder {
	private final UserRepository userRepository;

	@Override
	public User findById(Long id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
	}

    @Override
    public User findProxyById(Long id) {
        return userRepository.getReferenceById(id);
    }

    @Override
	public boolean existsById(Long id) {
		return userRepository.existsById(id);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmailAndDeletedAtIsNull(email);
	}

	@Override
	public boolean existsByNickName(String nickName) {
		return userRepository.existsByNicknameAndDeletedAtIsNull(nickName);
	}

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email).orElseThrow(
                ()-> new AppException(UserErrorCode.USER_NOT_FOUND));
    }

}
