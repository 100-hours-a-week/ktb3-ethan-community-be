package org.restapi.springrestapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.auth.SignUpRequest;
import org.restapi.springrestapi.dto.user.EncodedPassword;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserTest {
    @Mock
    PasswordEncoder passwordEncoder;

    User user;

    public static User simpleUser() {
        return User.builder()
                .nickname("nickname")
                .email("user@example.com")
                .password("testerQ1!")      // need encode password before save
                .profileImageUrl("http://user-profile-image")
                .build();
    }

    @BeforeEach
    void setUp() {
        user = this.simpleUser();
    }

    @Nested
    class create {
        @Test
        @DisplayName("빌더로 생성한 User는 posts/comments 컬렉션이 비어 있지만 null이 아니다")
        void builder_initializesCollections() {
            User user = UserTest.simpleUser();

            assertThat(user.getPosts()).isNotNull().isEmpty();
            assertThat(user.getComments()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("회원가입 요청으로 유저 생성 시 필드 매핑과 비밀번호 인코딩이 수행 후 저장된다.")
        void from_should_mapFields_and_encodePassword() {
            // given
            SignUpRequest request = new SignUpRequest(
                    user.getEmail(),
                    user.getPassword(),
                    user.getNickname(),
                    user.getProfileImageUrl()
            );
            final String ENCODED_PASSWORD = "encoded-password";
            given(passwordEncoder.encode(request.password())).willReturn(ENCODED_PASSWORD);

            // when
            User user = User.from(request, passwordEncoder);

            // then
            assertAll(
                    () -> verify(passwordEncoder).encode(request.password()),
                    () -> assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD),
                    () -> assertThat(user.getEmail()).isEqualTo(request.email()),
                    () -> assertThat(user.getNickname()).isEqualTo(request.nickname()),
                    () -> assertThat(user.getProfileImageUrl()).isEqualTo(request.profileImageUrl())
            );
        }
    }

    @Nested
    class update {
        @Test
        @DisplayName("닉네임과 새 이미지 URL이 정상 문자열이면 해당 값으로 변경된다")
        void updateProfile_validRequest_updatesNicknameAndImage() {
            // given
            PatchProfileRequest request =
                    new PatchProfileRequest("newNickname", "newProfileImageUrl", false);

            // when
            user.updateProfile(request);

            // then
            assertThat(user.getNickname()).isEqualTo(request.nickname());
            assertThat(user.getProfileImageUrl()).isEqualTo(request.profileImageUrl());
        }

        @Test
        @DisplayName("removeProfileImage=true이면 기존의 프로필 이미지 경로는 삭제(null)된다. ")
        void updateProfile_should_remove_image_when_flag_is_true() {
            // given
            PatchProfileRequest request =
                    new PatchProfileRequest("newNickname", "newProfileImageUrl", true);

            // when
            user.updateProfile(request);

            // then
            assertThat(user.getNickname()).isEqualTo(request.nickname());
            assertThat(user.getProfileImageUrl()).isNull();
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("프로필 이미지 URL이 null 또는 공백이면 기존 이미지를 유지한다")
    void updateProfile_nullOrBlankProfileImageUrl_keepsOldProfileImageUrl(String invalidProfileImageUrl) {
        // given
        final String PREV_PROFILE_IMAGE_URL = user.getProfileImageUrl();

        PatchProfileRequest request =
                new PatchProfileRequest("newNickname", invalidProfileImageUrl, false);

        // when
        user.updateProfile(request);

        // then
        assertThat(user.getNickname()).isEqualTo("newNickname");
        assertThat(user.getProfileImageUrl()).isEqualTo(PREV_PROFILE_IMAGE_URL);
    }

    @Test
    @DisplayName("새로운 비밀번호가 정상 문자열이면 인코딩 후 비밀번호가 변경된다")
    void updatePassword_validInput_encodesAndUpdates() {
        // given
        final String NEW_PASSWORD = "raw-password", ENCODED_PASSWORD = "encoded-password";
        given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(ENCODED_PASSWORD);

        // when
        user.updatePassword(new EncodedPassword(passwordEncoder.encode(NEW_PASSWORD)));

        // then
        verify(passwordEncoder).encode(NEW_PASSWORD);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
    }
}
