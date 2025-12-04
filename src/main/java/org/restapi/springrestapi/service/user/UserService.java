package org.restapi.springrestapi.service.user;

import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.dto.user.UserProfileResult;

public interface UserService {
	UserProfileResult getUserProfile(Long id) ;
	void updateProfile(Long id, PatchProfileRequest request);
	void updatePasswod(Long id, ChangePasswordRequest request);
	void deleteUser(Long id);
}
