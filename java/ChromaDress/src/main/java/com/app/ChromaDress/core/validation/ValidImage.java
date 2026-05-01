package com.app.ChromaDress.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImage {

  String message() default "Invalid image file. Only PNG and JPEG images are supported.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
