package org.restapi.springrestapi.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.restapi.springrestapi.dto.user.ChangePasswordRequest;
import org.restapi.springrestapi.exception.AppException;
import org.restapi.springrestapi.exception.code.AuthErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthValidatorTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthValidator authValidator;

    @Test
    @DisplayName("새 비밀번호와 확인 비밀번호가 다르면 NOT_MATCH_NEW_PASSWORD 예외를 던진다")
    void validateNewPassword_mismatch_throws() {
        ChangePasswordRequest request = new ChangePasswordRequest("Password1!", "Password2!");

        assertThatThrownBy(() -> authValidator.validateNewPassword(request, "encoded-old"))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(((AppException) ex).getErrorCode())
                .isEqualTo(AuthErrorCode.NOT_MATCH_NEW_PASSWORD));
    }

    @Test
    @DisplayName("새 비밀번호가 기존 비밀번호와 동일하면 PASSWORD_DUPLICATED 예외를 던진다")
    void validateNewPassword_duplicate_throws() {
        ChangePasswordRequest request = new ChangePasswordRequest("Password1!", "Password1!");
        given(passwordEncoder.matches(eq(request.password()), eq("encoded-old"))).willReturn(true);

        assertThatThrownBy(() -> authValidator.validateNewPassword(request, "encoded-old"))
            .isInstanceOf(AppException.class)
            .satisfies(ex -> org.assertj.core.api.Assertions.assertThat(((AppException) ex).getErrorCode())
                .isEqualTo(AuthErrorCode.PASSWORD_DUPLICATED));
    }

    @Test
    @DisplayName("새 비밀번호가 기존과 다르고 확인 비밀번호와 일치하면 예외를 던지지 않는다")
    void validateNewPassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest("Password1!", "Password1!");
        given(passwordEncoder.matches(eq(request.password()), eq("encoded-old"))).willReturn(false);

        authValidator.validateNewPassword(request, "encoded-old");
    }
}
