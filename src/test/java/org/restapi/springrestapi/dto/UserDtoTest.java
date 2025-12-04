package org.restapi.springrestapi.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.restapi.springrestapi.common.annotation.ValidEmail;
import org.restapi.springrestapi.common.annotation.ValidNickname;
import org.restapi.springrestapi.common.annotation.ValidPassword;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDtoTest {

    private Validator validator;
    private final static String
            VALID_RAW_PASSWORD = "testerQ1!",
            NICKNAME = "nickname",
            EMAIL = "email",
            PASSWORD = "password"
    ;
    record TestDto(
            @ValidNickname
            String nickname,

            @ValidEmail
            String email,

            @ValidPassword
            String password
    ) {}

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    class validNicknameTest {
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("닉네임이 null, 빈 문자열, 공백이면 검증에 실패한다")
        void nickname_validation_fail_blank(String invalidNickname) {
            // given
            TestDto dto = new TestDto(invalidNickname, null, null);

            // when
            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, NICKNAME);

            // then
            assertThat(violations).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "열한글자가넘어가는닉네임입니다"})
        @DisplayName("닉네임 길이가 2~10자가 아니면 검증에 실패한다")
        void nickname_validation_fail_length(String invalidNickname) {
            // given
            TestDto dto = new TestDto(invalidNickname, null, null);

            // when
            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, NICKNAME);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting("message")
                    .contains("닉네임은 2자 이상 10자 이하여야 합니다.");
        }

        @Test
        @DisplayName("닉네임에 공백이 섞여 있으면 검증에 실패한다 (@Pattern 확인)")
        void nickname_validation_fail_pattern() {
            // given
            TestDto dto = new TestDto("us er", null, null);

            // when
            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, NICKNAME);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting("message")
                    .contains("닉네임에는 공백을 포함할 수 없습니다.");
        }
    }

    @Nested
    class validEmailTest {
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("이메일: null, 빈 문자열, 공백이면 '필수 입력값' 메시지가 나온다")
        void email_blank_fail(String invalidEmail) {
            // given
            TestDto dto = new TestDto(null, invalidEmail, null);

            // when
            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, EMAIL);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting("message")
                    .contains("이메일은 필수 입력값입니다.");
        }
    }

    @Nested
    class validPasswordTest {
        @Test
        @DisplayName("비밀번호: 대문자+소문자+숫자+특수문자 포함 8~20자면 통과한다")
        void password_valid_pass() {
            // given
            TestDto dto = new TestDto(null, null, VALID_RAW_PASSWORD);

            // when
            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, PASSWORD);

            // then
            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        @DisplayName("비밀번호: null, 빈 문자열, 공백이면 '필수' 메시지가 나온다")
        void password_blank_fail(String invalidPassword) {
            // given
            TestDto dto = new TestDto(null, null, invalidPassword);

            // when
            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, PASSWORD);

            // then
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting("message")
                    .contains("비밀번호는 필수입니다.");
        }

        @ParameterizedTest
        @ValueSource(strings = {"Short1!", "TooLongPasswordThatExceedsLimit1!"}) // 7자, 21자 이상
        @DisplayName("비밀번호: 길이가 8~20자가 아니면 '길이 오류' 메시지가 나온다")
        void password_length_fail(String invalidPassword) {
            TestDto dto = new TestDto(null, null, invalidPassword);

            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, PASSWORD);

            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting("message")
                    .contains("비밀번호는 8자 이상 20자 이하여야 합니다.");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "weakpassword1!",  // 대문자 없음
                "WEAKPASSWORD1!",  // 소문자 없음
                "NoNumber!!!!!",   // 숫자 없음
                "NoSpecialChar1",  // 특수문자 없음
                "Space P@ss1"      // 공백 포함
        })
        @DisplayName("비밀번호: 복잡도 조건을 만족하지 않거나 공백이 있으면 '패턴 오류' 메시지가 나온다")
        void password_pattern_fail(String invalidPassword) {
            TestDto dto = new TestDto(null, null, invalidPassword);

            Set<ConstraintViolation<TestDto>> violations = validator.validateProperty(dto, PASSWORD);

            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting("message")
                    .contains("비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.");
        }
    }
}
