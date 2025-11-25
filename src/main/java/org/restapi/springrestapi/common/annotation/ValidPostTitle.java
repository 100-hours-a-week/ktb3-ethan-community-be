package org.restapi.springrestapi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "post_title_is_required")
@Size(max = 26, message = "invalid_password_length")
public @interface ValidPostTitle {
	String message() default "invalid_post_title";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
