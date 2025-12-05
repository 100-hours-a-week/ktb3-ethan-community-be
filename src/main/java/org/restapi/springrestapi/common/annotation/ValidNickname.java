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
@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
@Pattern(regexp = "^(?!\\s).*$", message = "닉네임은 공백으로 시작할 수 없습니다.")
public @interface ValidNickname {
    String message() default "유효하지 않은 닉네임 형식입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
