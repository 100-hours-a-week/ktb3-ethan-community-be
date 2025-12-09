package org.restapi.springrestapi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = ".*\\S.*", message = "게시글 제목은 공백일 수 없습니다.")
@Size(max = 26, message = "게시글 제목은 최대 26자까지 입력 가능합니다.")
public @interface ValidPostTitle {
    String message() default "유효하지 않은 게시글 제목입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
