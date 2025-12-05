package org.restapi.springrestapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.user.PatchProfileRequest;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserTest {

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
                    new PatchProfileRequest(null, "   https://img   ", false);

            // when
            user.updateProfile(request);

            // then
            assertThat(user.getProfileImageUrl()).isEqualTo("https://img");
        }

        @Test
        @DisplayName("removeProfileImage=true이면 기존의 프로필 이미지 경로는 삭제(null)된다. ")
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
}
