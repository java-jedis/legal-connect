package com.javajedis.legalconnect.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom password validator that provides detailed feedback about password requirements.
 */
@Slf4j
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        List<String> errors = new ArrayList<>();

        if (password.length() < 8) {
            errors.add("at least 8 characters");
        }
        if (password.length() > 100) {
            errors.add("no more than 100 characters");
        }

        if (!containsUpperCase(password)) {
            errors.add("one uppercase letter (A-Z)");
        }
        if (!containsLowerCase(password)) {
            errors.add("one lowercase letter (a-z)");
        }
        if (!containsDigit(password)) {
            errors.add("one number (0-9)");
        }
        if (!containsSpecial(password)) {
            errors.add("one special symbol (@#$%^&+=!*()_-[]{}|;:,.<>?)");
        }
        if (containsWhitespace(password)) {
            errors.add("no spaces");
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Password must contain: " + String.join(", ", errors);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean containsUpperCase(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsLowerCase(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDigit(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsSpecial(String s) {
        String specialChars = "@#$%^&+=!*()_-[]{}|;:,.<>?";
        for (char c : s.toCharArray()) {
            if (specialChars.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }

    private boolean containsWhitespace(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }
}
