package com.javajedis.legalconnect.common.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom annotation for password validation.
 * Validates that password meets security requirements.
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password does not meet security requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
