package org.restapi.springrestapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.dto.user.UserProfileResult;
import org.restapi.springrestapi.finder.UserFinder;
import org.restapi.springrestapi.model.User;
import org.restapi.springrestapi.model.UserTest;
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.service.user.UserServiceImpl;
import org.restapi.springrestapi.validator.AuthValidator;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;
    @Mock
    UserFinder userFinder;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserValidator userValidator;
    @Mock
    AuthValidator authValidator;

    @Test
    @DisplayName("유저 프로필 조회 시 Finder를 통해 조회 후 DTO로 반환한다")
    void getUserProfile_success() {
        // given
        Long userId = 1L;
        User user = UserTest.simpleUser();
        given(userFinder.findByIdOrThrow(userId)).willReturn(user);

        // when
        UserProfileResult result = userService.getUserProfile(userId);

        // then
        verify(userFinder).findByIdOrThrow(userId);
        assertThat(result.id()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("프로필 수정 시 중복 검사 후 정보를 업데이트하고 저장한다")
    void updateProfile_success() {
        // given
        Long userId = 1L;
        PatchProfileRequest request = new PatchProfileRequest("newNickname", "newProfileImageUrl", false);
        User user = UserTest.simpleUser();
        given(userFinder.findByIdOrThrow(userId)).willReturn(user);

        // when
        userService.updateProfile(userId, request);

        // then
        verify(userValidator).validateDuplicateNickname(request.nickname());
        verify(userFinder).findByIdOrThrow(userId);
        verify(userRepository).save(user);

        assertThat(user.getNickname()).isEqualTo(request.nickname());
    }

    @Test
    @DisplayName("비밀번호 변경 시 유효성 검사 후 인코딩하여 저장한다")
    void changePassword_success() {
        // given
        Long userId = 1L;
        final String NEW_PASSWORD = "newPassowrd", ENCODED_PASSWORD = "encodedNewPassword";
        ChangePasswordRequest request = new ChangePasswordRequest(NEW_PASSWORD, NEW_PASSWORD);
        User user = UserTest.simpleUser();

        given(userFinder.findByIdOrThrow(userId)).willReturn(user);
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        userService.updatePasswod(userId, request);

        // then
        verify(authValidator).validateNewPassword(NEW_PASSWORD, NEW_PASSWORD);
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("유저 삭제 시 Repository의 deleteById가 호출된다")
    void deleteUser_success() {
        // given
        Long userId = 1L;

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository).deleteById(userId);
    }
}
