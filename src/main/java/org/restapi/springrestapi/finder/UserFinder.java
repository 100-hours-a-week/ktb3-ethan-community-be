package org.restapi.springrestapi.finder;

import org.restapi.springrestapi.model.User;


public interface UserFinder {
	/**
	 * 사용자의 데이터베이스 id로 사용자를 조회합니다.
	 * @param id User 엔티티의 데이터베이스 id
	 * @return 조회된 사용자 엔티티를 반환합니다.
	 */
	User findById(Long id);
    User findProxyById(Long id);

	/**
	 * 사용자의 존재 여부만 확인합니다.
	 * 엔티티 로딩 없이 boolean으로 반환합니다.
	 */
	boolean existsById(Long id);

	/**
	 * 이메일 중복 여부를 확인합니다.
	 */
	boolean existsByEmail(String email);

	/**
	 * 닉네임 중복 여부를 확인합니다.
	 */
	boolean existsByNickName(String nickName);

    User findByEmail(String email);

    User findByEmailOrAuthThrow(String email);
}
