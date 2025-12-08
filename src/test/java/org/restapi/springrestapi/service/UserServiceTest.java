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
import org.restapi.springrestapi.repository.UserRepository;
import org.restapi.springrestapi.support.fixture.UserFixture;
import org.restapi.springrestapi.validator.AuthValidator;
import org.restapi.springrestapi.validator.UserValidator;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock UserRepository userRepository;
    @Mock UserFinder userFinder;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserValidator userValidator;
    @Mock AuthValidator authValidator;

    @Test
    @DisplayName("사용자 식별자로 조회 시 Finder 결과를 DTO로 반환한다")
    void getUserProfile_returnsProfileDto() {
		// given
        Long userId = 1L;
        User user = UserFixture.persistedUser().toBuilder()
            .id(userId)
            .build();
        given(userFinder.findByIdOrThrow(userId)).willReturn(user);

		// when
        UserProfileResult result = userService.getUserProfile(userId);

		// then
        verify(userFinder).findByIdOrThrow(userId);
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.nickname()).isEqualTo(user.getNickname());
    }

    @Test
    @DisplayName("프로필 수정 시 닉네임 중복 검증 후 사용자 정보를 갱신하고 저장한다")
    void updateProfile_updatesEntityAndPersistsIt() {
		// given
        Long userId = 1L;
        PatchProfileRequest request = new PatchProfileRequest("새닉네임", "https://img/new", false);
        User user = UserFixture.persistedUser().toBuilder()
            .id(userId)
            .build();
        given(userFinder.findByIdOrThrow(userId)).willReturn(user);

		// when
        userService.updateProfile(userId, request);

		// then
        verify(userValidator).validateDuplicateNickname(request.nickname());
        verify(userFinder).findByIdOrThrow(userId);
        verify(userRepository).save(user);
        assertThat(user.getNickname()).isEqualTo(request.nickname());
        assertThat(user.getProfileImageUrl()).isEqualTo(request.profileImageUrl());
    }

    @Test
    @DisplayName("비밀번호 변경 시 새 비밀번호 검증과 인코딩을 거쳐 저장한다")
    void updatePassword_validRequest_encodesAndSavesPassword() {
		// given
        User user = UserFixture.persistedUser().toBuilder()
            .id(1L)
            .password("OldPassword1!")
            .build();
        ChangePasswordRequest request = new ChangePasswordRequest("NewPassword1!", "NewPassword1!");
        final String encodedPassword = "encoded-password";
        given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);

		// when
        String currentPassword = user.getPassword();
        userService.updatePassword(user, request);

		// then
        verify(authValidator).validateNewPassword(request, currentPassword);
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
    }
}
