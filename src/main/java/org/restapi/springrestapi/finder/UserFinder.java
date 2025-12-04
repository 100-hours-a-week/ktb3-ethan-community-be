package org.restapi.springrestapi.finder;

import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.restapi.springrestapi.exception.code.ErrorCode;
import org.restapi.springrestapi.exception.code.UserErrorCode;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.repository.UserRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFinder {
	private final UserRepository userRepository;

    public User findProxyById(Long id) {
        return userRepository.getReferenceById(id);
    }

	public User findByIdOrNull(Long id) {
		return userRepository.findById(id)
                .orElse(null);
	}

    private User findByIdOrThrow(Long id, ErrorCode errorCode) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(errorCode));
    }

    public User findByIdOrThrow(Long id) {
        return findByIdOrThrow(id, UserErrorCode.USER_NOT_FOUND);
    }

    public User findByIdOrAuthThrow(Long id) {
        return findByIdOrThrow(id, AuthErrorCode.UNAUTHORIZED);
    }

	public boolean existsById(Long id) {
		return userRepository.existsById(id);
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public boolean existsByNickName(String nickName) {
		return userRepository.existsByNickname(nickName);
	}


    public User findByEmailOrAuthThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                ()-> new AppException(AuthErrorCode.INVALID_EMAIL_OR_PASSWORD)
        );
    }
}
