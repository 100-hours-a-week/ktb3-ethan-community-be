package org.restapi.springrestapi.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restapi.springrestapi.common.annotation.ValidPassword;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidPasswordTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    record TestDto(
            @ValidPassword
            String password
    ) {}

    @Test
    @DisplayName("대문자/소문자/숫자/특수문자를 모두 포함한 8~20자 비밀번호는 통과한다")
    void validPassword_passes() {
        TestDto dto = new TestDto("GoodPass1!");

        Set<ConstraintViolation<TestDto>> violations = VALIDATOR.validate(dto);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Short1!", "WayTooLongPassword123!"})
    @DisplayName("비밀번호 길이가 범위를 벗어나면 길이 제약에 실패한다")
    void passwordLength_fails(String invalidPassword) {
        TestDto dto = new TestDto(invalidPassword);

        Set<ConstraintViolation<TestDto>> violations = VALIDATOR.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting("message")
                .contains("비밀번호는 8자 이상 20자 이하여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "alllowercase1!",   // 대문자 없음
            "ALLUPPERCASE1!",   // 소문자 없음
            "NoNumber!!!!",     // 숫자 없음
            "NoSpecialChar1",   // 특수문자 없음
            "Space P@ss1",      // 허용되지 않는 공백 포함
            "", "   "
    })
    @DisplayName("복잡도 요건을 만족하지 않으면 패턴 제약에 실패한다")
    void passwordPattern_fails(String invalidPassword) {
        TestDto dto = new TestDto(invalidPassword);

        Set<ConstraintViolation<TestDto>> violations = VALIDATOR.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting("message")
                .contains("비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.");
    }
}
