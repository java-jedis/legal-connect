package com.javajedis.legalconnect.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprehensive unit tests for PasswordValidator.
 * Tests all password validation rules and edge cases to achieve 90%+ coverage.
 */
@DisplayName("PasswordValidator Tests")
class PasswordValidatorTest {

    private PasswordValidator passwordValidator;
    private ConstraintValidatorContext context;
    private ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintViolationBuilder.class);
        
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    // VALID PASSWORD TESTS

    @Test
    @DisplayName("Should validate correct password with all requirements")
    void isValid_ValidPassword_ReturnsTrue() {
        String validPassword = "MySecure123!";
        
        boolean result = passwordValidator.isValid(validPassword, context);
        
        assertTrue(result);
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("Should validate password with minimum length")
    void isValid_MinimumLengthPassword_ReturnsTrue() {
        String validPassword = "Abc123!@";  // exactly 8 characters
        
        boolean result = passwordValidator.isValid(validPassword, context);
        
        assertTrue(result);
        // Verify that the password meets the minimum length requirement
        assertEquals(8, validPassword.length(), "Password should be exactly 8 characters");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("Should validate password with maximum length")
    void isValid_MaximumLengthPassword_ReturnsTrue() {
        // Create a 100-character password
        String validPassword = "A".repeat(90) + "bc123!@#$%";  // 100 characters total
        
        boolean result = passwordValidator.isValid(validPassword, context);
        
        assertTrue(result);
        // Verify that the password meets the maximum length requirement
        assertEquals(100, validPassword.length(), "Password should be exactly 100 characters");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "MyPassword123!",
        "SecurePass1@",
        "Complex#Pass9",
        "Valid$123Pass",
        "Strong&Pass7"
    })
    @DisplayName("Should validate various valid password formats")
    void isValid_VariousValidPasswords_ReturnsTrue(String password) {
        boolean result = passwordValidator.isValid(password, context);
        
        assertTrue(result);
    }

    @Test
    @DisplayName("Should validate password with all special characters")
    void isValid_AllSpecialCharacters_ReturnsTrue() {
        String passwordWithAllSpecials = "Pass123@#$%^&+=!*()_-[]{}|;:,.<>?";
        
        boolean result = passwordValidator.isValid(passwordWithAllSpecials, context);
        
        assertTrue(result);
        // Verify that the password contains all allowed special characters
        String specialChars = "@#$%^&+=!*()_-[]{}|;:,.<>?";
        boolean containsAllSpecials = specialChars.chars().allMatch(c -> passwordWithAllSpecials.indexOf(c) != -1);
        assertTrue(containsAllSpecials, "Password should contain all special characters");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    // NULL AND EMPTY PASSWORD TESTS

    @Test
    @DisplayName("Should reject null password")
    void isValid_NullPassword_ReturnsFalse() {
        boolean result = passwordValidator.isValid(null, context);
        
        assertFalse(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t\n\r"})
    @DisplayName("Should reject empty and whitespace-only passwords")
    void isValid_EmptyAndWhitespacePasswords_ReturnsFalse(String invalidPassword) {
        boolean result = passwordValidator.isValid(invalidPassword, context);
        
        assertFalse(result);
    }

    // LENGTH VALIDATION TESTS

    @Test
    @DisplayName("Should reject password shorter than 8 characters")
    void isValid_TooShortPassword_ReturnsFalse() {
        String shortPassword = "Abc12!";  // 6 characters
        
        boolean result = passwordValidator.isValid(shortPassword, context);
        
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Password must contain: at least 8 characters");
    }

    @Test
    @DisplayName("Should reject password longer than 100 characters")
    void isValid_TooLongPassword_ReturnsFalse() {
        String longPassword = "A".repeat(95) + "bc123!@";  // 101 characters
        
        boolean result = passwordValidator.isValid(longPassword, context);
        
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("Password must contain: no more than 100 characters");
    }

    // UPPERCASE VALIDATION TESTS

    @Test
    @DisplayName("Should reject password without uppercase letter")
    void isValid_NoUppercasePassword_ReturnsFalse() {
        String noUppercasePassword = "mypassword123!";
        
        boolean result = passwordValidator.isValid(noUppercasePassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: one uppercase letter (A-Z)");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Mypassword123!",    // uppercase at beginning
        "mypassword123!Z",   // uppercase at end
        "myPASSWORD123!",    // lowercase at beginning
        "MYPASSWORD123!z"    // lowercase at end
    })
    @DisplayName("Should validate passwords with uppercase and lowercase letters in various positions")
    void isValid_UppercaseAndLowercasePositions_ReturnsTrue(String password) {
        boolean result = passwordValidator.isValid(password, context);
        
        assertTrue(result);
        // Verify that the password contains both uppercase and lowercase letters
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        assertTrue(hasUppercase, "Password should contain at least one uppercase letter");
        assertTrue(hasLowercase, "Password should contain at least one lowercase letter");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    // LOWERCASE VALIDATION TESTS

    @Test
    @DisplayName("Should reject password without lowercase letter")
    void isValid_NoLowercasePassword_ReturnsFalse() {
        String noLowercasePassword = "MYPASSWORD123!";
        
        boolean result = passwordValidator.isValid(noLowercasePassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: one lowercase letter (a-z)");
    }

    // DIGIT VALIDATION TESTS

    @Test
    @DisplayName("Should reject password without digit")
    void isValid_NoDigitPassword_ReturnsFalse() {
        String noDigitPassword = "MyPassword!";
        
        boolean result = passwordValidator.isValid(noDigitPassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: one number (0-9)");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"})
    @DisplayName("Should validate password with each digit")
    void isValid_WithEachDigit_ReturnsTrue(String digit) {
        String password = "MyPassword" + digit + "!";
        
        boolean result = passwordValidator.isValid(password, context);
        
        assertTrue(result);
        // Verify that the password contains the specific digit
        assertTrue(password.contains(digit), "Password should contain the digit: " + digit);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        assertTrue(hasDigit, "Password should contain at least one digit");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1MyPassword!",    // digit at beginning
        "MyPassword!9"     // digit at end
    })
    @DisplayName("Should validate passwords with digits in various positions")
    void isValid_DigitPositions_ReturnsTrue(String password) {
        boolean result = passwordValidator.isValid(password, context);
        
        assertTrue(result);
        // Verify that the password contains at least one digit
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        assertTrue(hasDigit, "Password should contain at least one digit");
        // Verify digit position (either at start or end)
        boolean digitAtStart = Character.isDigit(password.charAt(0));
        boolean digitAtEnd = Character.isDigit(password.charAt(password.length() - 1));
        assertTrue(digitAtStart || digitAtEnd, "Password should have digit at beginning or end");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    // SPECIAL CHARACTER VALIDATION TESTS

    @Test
    @DisplayName("Should reject password without special character")
    void isValid_NoSpecialCharPassword_ReturnsFalse() {
        String noSpecialPassword = "MyPassword123";
        
        boolean result = passwordValidator.isValid(noSpecialPassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: one special symbol (@#$%^&+=!*()_-[]{}|;:,.<>?)");
    }

    @ParameterizedTest
    @ValueSource(strings = {"@", "#", "$", "%", "^", "&", "+", "=", "!", "*", "(", ")", "_", "-", "[", "]", "{", "}", "|", ";", ":", ",", ".", "<", ">", "?"})
    @DisplayName("Should validate password with each special character")
    void isValid_WithEachSpecialChar_ReturnsTrue(String specialChar) {
        String password = "MyPassword123" + specialChar;
        
        boolean result = passwordValidator.isValid(password, context);
        
        assertTrue(result);
        // Verify that the password contains the specific special character
        assertTrue(password.contains(specialChar), "Password should contain the special character: " + specialChar);
        // Verify that the password contains at least one special character
        String allowedSpecialChars = "@#$%^&+=!*()_-[]{}|;:,.<>?";
        boolean hasSpecialChar = password.chars().anyMatch(c -> allowedSpecialChars.indexOf(c) != -1);
        assertTrue(hasSpecialChar, "Password should contain at least one special character");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "@MyPassword123",    // special character at beginning
        "MyPassword123!"     // special character at end
    })
    @DisplayName("Should validate passwords with special characters in various positions")
    void isValid_SpecialCharPositions_ReturnsTrue(String password) {
        boolean result = passwordValidator.isValid(password, context);
        
        assertTrue(result);
        // Verify that the password contains at least one special character
        String allowedSpecialChars = "@#$%^&+=!*()_-[]{}|;:,.<>?";
        boolean hasSpecialChar = password.chars().anyMatch(c -> allowedSpecialChars.indexOf(c) != -1);
        assertTrue(hasSpecialChar, "Password should contain at least one special character");
        // Verify special character position (either at start or end)
        boolean specialAtStart = allowedSpecialChars.indexOf(password.charAt(0)) != -1;
        boolean specialAtEnd = allowedSpecialChars.indexOf(password.charAt(password.length() - 1)) != -1;
        assertTrue(specialAtStart || specialAtEnd, "Password should have special character at beginning or end");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    // WHITESPACE VALIDATION TESTS

    @ParameterizedTest
    @ValueSource(strings = {
        "My Password123!",    // space in middle
        "My\tPassword123!",   // tab in middle
        "My\nPassword123!",   // newline in middle
        "My\rPassword123!",   // carriage return in middle
        " MyPassword123!",    // leading space
        "MyPassword123! "     // trailing space
    })
    @DisplayName("Should reject passwords with whitespace characters")
    void isValid_PasswordsWithWhitespace_ReturnsFalse(String passwordWithWhitespace) {
        boolean result = passwordValidator.isValid(passwordWithWhitespace, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: no spaces");
    }

    // MULTIPLE ERRORS TESTS

    @Test
    @DisplayName("Should reject password with multiple errors and show all requirements")
    void isValid_MultipleErrors_ReturnsFalseWithAllErrors() {
        String badPassword = "abc";  // too short, no uppercase, no digit, no special char
        
        boolean result = passwordValidator.isValid(badPassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Password must contain: at least 8 characters, one uppercase letter (A-Z), one number (0-9), one special symbol (@#$%^&+=!*()_-[]{}|;:,.<>?)"
        );
    }

    @Test
    @DisplayName("Should reject password with all possible errors")
    void isValid_AllErrors_ReturnsFalseWithAllErrorMessages() {
        String terriblePassword = "a b";  // too short, no uppercase, no digit, no special char, has space
        
        boolean result = passwordValidator.isValid(terriblePassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Password must contain: at least 8 characters, one uppercase letter (A-Z), one number (0-9), one special symbol (@#$%^&+=!*()_-[]{}|;:,.<>?), no spaces"
        );
    }

    @Test
    @DisplayName("Should reject password that is too long with other errors")
    void isValid_TooLongWithOtherErrors_ReturnsFalseWithAllErrors() {
        String longBadPassword = "a".repeat(101);  // too long, no uppercase, no digit, no special char
        
        boolean result = passwordValidator.isValid(longBadPassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Password must contain: no more than 100 characters, one uppercase letter (A-Z), one number (0-9), one special symbol (@#$%^&+=!*()_-[]{}|;:,.<>?)"
        );
    }

    // EDGE CASES

    @Test
    @DisplayName("Should handle password with only numbers")
    void isValid_OnlyNumbers_ReturnsFalse() {
        String onlyNumbers = "12345678";
        
        boolean result = passwordValidator.isValid(onlyNumbers, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Password must contain: one uppercase letter (A-Z), one lowercase letter (a-z), one special symbol (@#$%^&+=!*()_-[]{}|;:,.<>?)"
        );
    }

    @Test
    @DisplayName("Should handle password with only letters")
    void isValid_OnlyLetters_ReturnsFalse() {
        String onlyLetters = "MyPassword";
        
        boolean result = passwordValidator.isValid(onlyLetters, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Password must contain: one number (0-9), one special symbol (@#$%^&+=!*()_-[]{}|;:,.<>?)"
        );
    }

    @Test
    @DisplayName("Should handle password with only special characters")
    void isValid_OnlySpecialChars_ReturnsFalse() {
        String onlySpecial = "!@#$%^&*";
        
        boolean result = passwordValidator.isValid(onlySpecial, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate(
            "Password must contain: one uppercase letter (A-Z), one lowercase letter (a-z), one number (0-9)"
        );
    }

    @Test
    @DisplayName("Should handle password with Unicode characters")
    void isValid_UnicodeCharacters_ReturnsFalse() {
        String unicodePassword = "Mÿ Pässwörd123!";  // contains non-ASCII characters and space
        
        boolean result = passwordValidator.isValid(unicodePassword, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: no spaces");
    }

    @Test
    @DisplayName("Should validate password with mixed case and numbers")
    void isValid_MixedCaseAndNumbers_ReturnsTrue() {
        String mixedPassword = "AbC123dEf456!";
        
        boolean result = passwordValidator.isValid(mixedPassword, context);
        
        assertTrue(result);
        verify(context, never()).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Should validate password with multiple special characters")
    void isValid_MultipleSpecialChars_ReturnsTrue() {
        String multiSpecialPassword = "MyPass123!@#$%";
        
        boolean result = passwordValidator.isValid(multiSpecialPassword, context);
        
        assertTrue(result);
        // Verify that no constraint violations are triggered for valid password
        verify(context, never()).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }

    // BOUNDARY TESTS

    @Test
    @DisplayName("Should reject password with exactly 7 characters")
    void isValid_SevenCharacters_ReturnsFalse() {
        String sevenChars = "Abc123!";
        
        boolean result = passwordValidator.isValid(sevenChars, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: at least 8 characters");
    }

    @Test
    @DisplayName("Should validate password with exactly 8 characters")
    void isValid_EightCharacters_ReturnsTrue() {
        String eightChars = "Abc1234!";
        
        boolean result = passwordValidator.isValid(eightChars, context);
        
        assertTrue(result);
        // Verify that the password meets the exact minimum length boundary
        assertEquals(8, eightChars.length(), "Password should be exactly 8 characters for boundary test");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("Should validate password with exactly 99 characters")
    void isValid_NinetyNineCharacters_ReturnsTrue() {
        String ninetyNineChars = "A".repeat(89) + "bc123!@#$%";  // 99 characters
        
        boolean result = passwordValidator.isValid(ninetyNineChars, context);
        
        assertTrue(result);
        // Verify that the password is within the maximum length boundary
        assertEquals(99, ninetyNineChars.length(), "Password should be exactly 99 characters for boundary test");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("Should validate password with exactly 100 characters")
    void isValid_OneHundredCharacters_ReturnsTrue() {
        String oneHundredChars = "A".repeat(90) + "bc123!@#$%";  // 100 characters
        
        boolean result = passwordValidator.isValid(oneHundredChars, context);
        
        assertTrue(result);
        // Verify that the password meets the exact maximum length boundary
        assertEquals(100, oneHundredChars.length(), "Password should be exactly 100 characters for boundary test");
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("Should reject password with exactly 101 characters")
    void isValid_OneHundredOneCharacters_ReturnsFalse() {
        String oneHundredOneChars = "A".repeat(91) + "bc123!@#$%";  // 101 characters
        
        boolean result = passwordValidator.isValid(oneHundredOneChars, context);
        
        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("Password must contain: no more than 100 characters");
    }
}