package org.restapi.springrestapi.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restapi.springrestapi.common.annotation.ValidNickname;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidNicknameTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    record TestDto(
            @ValidNickname
            String nickname
    ) {}

    @ParameterizedTest
    @ValueSource(strings = {"01", "0123456789"})
    @DisplayName("닉네임은 2~10자의 공백 없는 문자열이어야 한다.")
    void validNickname_passes(String validNickname) {
        TestDto dto = new TestDto(validNickname);

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "열한글자가넘어가는닉네임입니다"})
    @DisplayName("2~10자 범위를 벗어난 닉네임은 허용하지 않는다.")
    void nicknameLength_fails(String invalidNickname) {
        TestDto dto = new TestDto(invalidNickname);

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting("message")
                .contains("닉네임은 2자 이상 10자 이하여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {" nickname", "\tuser", "\n닉네임"})
    @DisplayName("닉네임이 공백으로 시작하면 패턴 제약에 실패한다")
    void nicknameLeadingWhitespace_fails(String invalidNickname) {
        TestDto dto = new TestDto(invalidNickname);

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting("message")
                .contains("닉네임은 공백으로 시작할 수 없습니다.");
    }

    @Test
    @DisplayName("중간에 공백이 포함된 닉네임은 허용된다")
    void nicknameInnerWhitespace_passes() {
        TestDto dto = new TestDto("user name");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
