package org.restapi.springrestapi.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.UserErrorCode;
import org.restapi.springrestapi.finder.UserFinder;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    UserFinder userFinder;

    @InjectMocks
    UserValidator userValidator;

    @Test
    @DisplayName("이메일이 중복되면 EMAIL_DUPLICATED 예외를 던진다")
    void validateDuplicateEmail_throwsWhenDuplicated() {
        given(userFinder.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> userValidator.validateDuplicateEmail("dup@test.com"))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(((AppException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.EMAIL_DUPLICATED));
    }

    @Test
    @DisplayName("닉네임이 중복되면 NICKNAME_DUPLICATED 예외를 던진다")
    void validateDuplicateNickname_throwsWhenDuplicated() {
        given(userFinder.existsByNickName("dupNick")).willReturn(true);

        assertThatThrownBy(() -> userValidator.validateDuplicateNickname("dupNick"))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(((AppException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.NICKNAME_DUPLICATED));
    }

    @Test
    @DisplayName("회원가입 검증은 이메일과 닉네임 중복을 모두 확인한다")
    void validateSignUpUser_checksBoth() {
        given(userFinder.existsByEmail("user@test.com")).willReturn(false);
        given(userFinder.existsByNickName("nickname")).willReturn(false);

        userValidator.validateSignUpUser("user@test.com", "nickname");

        verify(userFinder).existsByEmail("user@test.com");
        verify(userFinder).existsByNickName("nickname");
    }
}
