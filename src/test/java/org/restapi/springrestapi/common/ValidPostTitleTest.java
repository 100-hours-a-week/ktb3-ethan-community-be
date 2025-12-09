package org.restapi.springrestapi.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restapi.springrestapi.common.annotation.ValidPostTitle;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidPostTitleTest {

    private Validator validator;

    record TestDto(
            @ValidPostTitle
            String title
    ) {}

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @Test
    @DisplayName("공백을 포함한 정상적인 게시글 제목은 허용한다.")
    void valid_title_pass() {
        // given
        TestDto dto = new TestDto("공백을 포함한 게시글 제목");

        // when
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("PATCH 요청 시 게시글 제목은 null일 수 있다.")
    void null_title_pass() {
        // given
        TestDto dto = new TestDto(null);

        // when
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "  \t  ", "\n"})
    @DisplayName("게시글 제목이 빈 문자열이거나 공백만 있으면 검증에 실패한다")
    void blank_title_fail(String invalidTitle) {
        // given
        TestDto dto = new TestDto(invalidTitle);

        // when
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting("message")
                .contains("게시글 제목은 공백일 수 없습니다.");
    }

    @Test
    @DisplayName("게시글 제목 길이는 26자를 초과할 수 없다.")
    void length_fail() {
        // given
        String longTitle = "A".repeat(27); // 27자 생성
        TestDto dto = new TestDto(longTitle);

        // when
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting("message")
                .contains("게시글 제목은 최대 26자까지 입력 가능합니다.");
    }

    @Test
    @DisplayName("게시글 제목 길이는 26자까지 허용한다.")
    void length_boundary_pass() {
        // given
        String maxTitle = "A".repeat(26); // 26자 생성
        TestDto dto = new TestDto(maxTitle);

        // when
        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }
}
