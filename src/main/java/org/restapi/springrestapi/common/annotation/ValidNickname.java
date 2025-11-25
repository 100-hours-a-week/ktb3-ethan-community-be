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
@NotBlank(message = "nickname_is_required")
@Size(max = 10, message = "nickname_exceeds_max_length")
@Pattern(regexp = "\\S+", message = "nickname_must_not_contain_whitespace")
public @interface ValidNickname {
	String message() default "invalid_nickname";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
