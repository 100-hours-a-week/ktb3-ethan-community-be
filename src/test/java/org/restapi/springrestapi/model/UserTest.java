package org.restapi.springrestapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.auth.SignUpRequest;
import org.restapi.springrestapi.dto.user.EncodedPassword;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import org.restapi.springrestapi.support.fixture.UserFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    User user;

    @BeforeEach
    void setUp() {
        user = UserFixture.persistedUser();
    }

    @Nested
	@DisplayName("생성 테스트")
    class Factory {
        @Test
        @DisplayName("회원가입 요청과 인코딩된 비밀번호로 User.from을 호출하면 필드가 매핑된다")
        void from_validInputs_createsUserWithSignupFields() {
            // given
            SignUpRequest request = new SignUpRequest(
                "user@user.com",
                "Password1!",
                "nickname",
                "http://img"
            );
            EncodedPassword encodedPassword = new EncodedPassword("encoded");

            // when
            User created = User.from(request, encodedPassword);

            // then
            assertThat(created.getEmail()).isEqualTo(request.email());
            assertThat(created.getNickname()).isEqualTo(request.nickname());
            assertThat(created.getPassword()).isEqualTo(encodedPassword.value());
            assertThat(created.getProfileImageUrl()).isEqualTo(request.profileImageUrl());
        }

		@Test
		@DisplayName("빌더로 생성한 User는 posts/comments 컬렉션이 비어 있지만 null이 아니다")
		void builder_initializesCollections() {
			// given
			User user = User.builder().build();

			// then
			assertThat(user.getPosts()).isNotNull().isEmpty();
			assertThat(user.getComments()).isNotNull().isEmpty();
		}

        @Test
        @DisplayName("회원가입 요청이 null이면 User.from은 NullPointerException을 던진다")
        void from_nullRequest_throwsException() {
			// given
            EncodedPassword encodedPassword = new EncodedPassword("encoded");

			// when & then
            assertThatThrownBy(() -> User.from(null, encodedPassword))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class UpdateProfile {
        @Nested
        class Success {
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
            @DisplayName("닉네임이 null이면 기존 닉네임을 유지한다")
            void updateProfile_nullNickname_keepsOriginal() {
                // given
                String previous = user.getNickname();
                PatchProfileRequest request =
                    new PatchProfileRequest(null, "https://img", false);

                // when
                user.updateProfile(request);

                // then
                assertThat(user.getNickname()).isEqualTo(previous);
                assertThat(user.getProfileImageUrl()).isEqualTo("https://img");
            }

            @Test
            @DisplayName("프로필 이미지 저장경로 양끝에 포함된 공백은 trim 되어 저장된다")
            void updateProfile_trimsNicknameAndImage() {
                // given
                PatchProfileRequest request =
                    new PatchProfileRequest(null, "   http://img   ", false);

                // when
                user.updateProfile(request);

                // then
                assertThat(user.getProfileImageUrl()).isEqualTo("http://img");
            }

            @Test
            @DisplayName("removeProfileImage=true이면 기존의 프로필 이미지 경로는 삭제(null)된다.")
            void updateProfile_should_remove_image_when_flag_is_true() {
                // given
                PatchProfileRequest request =
                    new PatchProfileRequest(user.getNickname(), user.getProfileImageUrl(), true);

                // when
                user.updateProfile(request);

                // then
                assertThat(user.getNickname()).isEqualTo(request.nickname());
                assertThat(user.getProfileImageUrl()).isNull();
            }

            @Test
            @DisplayName("removeProfileImage=false이고 새 이미지가 null이면 기존 이미지를 유지한다")
            void updateProfile_nullImage_keepsOriginal() {
                // given
                String previousImage = user.getProfileImageUrl();
                PatchProfileRequest request =
                    new PatchProfileRequest("newNickname", null, false);

                // when
                user.updateProfile(request);

                // then
                assertThat(user.getProfileImageUrl()).isEqualTo(previousImage);
                assertThat(user.getNickname()).isEqualTo("newNickname");
            }
        }

        @Nested
        class Failure {
            @Test
            @DisplayName("PatchProfileRequest가 null이면 NullPointerException을 던진다")
            void updateProfile_nullRequest_throwsException() {
                assertThatThrownBy(() -> user.updateProfile(null))
                    .isInstanceOf(NullPointerException.class);
            }
        }
    }

    @Nested
    class UpdatePassword {
        @Test
        @DisplayName("EncodedPassword가 주어지면 패스워드가 해당 값으로 교체된다")
        void updatePassword_validInput_replacesPassword() {
            // given
            EncodedPassword encodedPassword = new EncodedPassword("encoded");

            // when
            user.updatePassword(encodedPassword);

            // then
            assertThat(user.getPassword()).isEqualTo(encodedPassword.value());
        }

        @Test
        @DisplayName("EncodedPassword가 null이면 NullPointerException을 던진다")
        void updatePassword_nullInput_throwsException() {
            assertThatThrownBy(() -> user.updatePassword(null))
                .isInstanceOf(NullPointerException.class);
        }
    }
}
