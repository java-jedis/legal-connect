package com.javajedis.legalconnect.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Unit tests for RequireUserOrVerifiedLawyer annotation.
 * Verifies annotation configuration, metadata, and complex security expression.
 */
@DisplayName("RequireUserOrVerifiedLawyer Annotation Tests")
class RequireUserOrVerifiedLawyerTest {

    private static final String EXPECTED_EXPRESSION = "hasRole('USER') or @lawyerVerificationChecker.isVerifiedLawyer()";

    @Nested
    @DisplayName("Annotation Configuration Tests")
    class AnnotationConfigurationTests {

        @Test
        @DisplayName("Should have correct target types")
        void shouldHaveCorrectTargetTypes() {
            // Given
            Target target = RequireUserOrVerifiedLawyer.class.getAnnotation(Target.class);

            // Then
            assertNotNull(target, "Target annotation should be present");
            assertEquals(2, target.value().length, "Should target exactly 2 element types");
            assertTrue(containsElementType(target.value(), ElementType.METHOD), "Should target METHOD");
            assertTrue(containsElementType(target.value(), ElementType.TYPE), "Should target TYPE");
        }

        @Test
        @DisplayName("Should have runtime retention policy")
        void shouldHaveRuntimeRetentionPolicy() {
            // Given
            Retention retention = RequireUserOrVerifiedLawyer.class.getAnnotation(Retention.class);

            // Then
            assertNotNull(retention, "Retention annotation should be present");
            assertEquals(RetentionPolicy.RUNTIME, retention.value(), 
                "Should have RUNTIME retention for Spring Security to process");
        }

        @Test
        @DisplayName("Should have correct PreAuthorize expression")
        void shouldHaveCorrectPreAuthorizeExpression() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);

            // Then
            assertNotNull(preAuthorize, "PreAuthorize annotation should be present");
            assertEquals(EXPECTED_EXPRESSION, preAuthorize.value(), 
                "Should use correct OR expression combining role check and bean method");
        }

        private boolean containsElementType(ElementType[] elementTypes, ElementType target) {
            for (ElementType elementType : elementTypes) {
                if (elementType == target) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nested
    @DisplayName("Annotation Application Tests")
    class AnnotationApplicationTests {

        @Test
        @DisplayName("Should be applicable to methods")
        void shouldBeApplicableToMethods() throws NoSuchMethodException {
            // Given
            Method testMethod = TestController.class.getMethod("userOrVerifiedLawyerMethod");

            // When
            RequireUserOrVerifiedLawyer annotation = testMethod.getAnnotation(RequireUserOrVerifiedLawyer.class);

            // Then
            assertNotNull(annotation, "Annotation should be present on method");
        }

        @Test
        @DisplayName("Should be applicable to classes")
        void shouldBeApplicableToClasses() {
            // Given
            Class<TestMixedAccessController> testClass = TestMixedAccessController.class;

            // When
            RequireUserOrVerifiedLawyer annotation = testClass.getAnnotation(RequireUserOrVerifiedLawyer.class);

            // Then
            assertNotNull(annotation, "Annotation should be present on class");
        }

        @Test
        @DisplayName("Should inherit PreAuthorize expression when applied")
        void shouldInheritPreAuthorizeExpressionWhenApplied() throws NoSuchMethodException {
            // Given
            Method testMethod = TestController.class.getMethod("userOrVerifiedLawyerMethod");

            // When
            RequireUserOrVerifiedLawyer requireAnnotation = testMethod.getAnnotation(RequireUserOrVerifiedLawyer.class);
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);

            // Then
            assertNotNull(requireAnnotation, "RequireUserOrVerifiedLawyer should be present");
            assertNotNull(preAuthorize, "PreAuthorize should be inherited");
            assertEquals(EXPECTED_EXPRESSION, preAuthorize.value(),
                "Should contain correct complex security expression");
        }
    }

    @Nested
    @DisplayName("Security Expression Validation Tests")
    class SecurityExpressionValidationTests {

        @Test
        @DisplayName("Should contain USER role check")
        void shouldContainUserRoleCheck() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            assertTrue(expression.contains("hasRole('USER')"), 
                "Should contain USER role check");
        }

        @Test
        @DisplayName("Should contain verified lawyer bean check")
        void shouldContainVerifiedLawyerBeanCheck() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            assertTrue(expression.contains("@lawyerVerificationChecker.isVerifiedLawyer()"), 
                "Should contain verified lawyer bean method call");
        }

        @Test
        @DisplayName("Should use OR logical operator")
        void shouldUseOrLogicalOperator() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            assertTrue(expression.contains(" or "), "Should use OR logical operator");
            assertTrue(!expression.contains(" and "), "Should not use AND logical operator");
        }

        @Test
        @DisplayName("Should have correct expression structure")
        void shouldHaveCorrectExpressionStructure() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            // Verify structure: "hasRole('USER') or @lawyerVerificationChecker.isVerifiedLawyer()"
            String[] parts = expression.split(" or ");
            assertEquals(2, parts.length, "Should have exactly 2 parts separated by OR");
            assertEquals("hasRole('USER')", parts[0].trim(), "First part should be USER role check");
            assertEquals("@lawyerVerificationChecker.isVerifiedLawyer()", parts[1].trim(), 
                "Second part should be verified lawyer check");
        }

        @Test
        @DisplayName("Should reference correct Spring bean")
        void shouldReferenceCorrectSpringBean() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            assertTrue(expression.contains("@lawyerVerificationChecker"), 
                "Should reference lawyerVerificationChecker bean");
            assertTrue(expression.contains("isVerifiedLawyer()"), 
                "Should call isVerifiedLawyer method");
        }

        @Test
        @DisplayName("Should allow multiple access paths")
        void shouldAllowMultipleAccessPaths() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            // Verify the expression allows two distinct access paths
            assertTrue(expression.contains("hasRole('USER')"), 
                "Should allow access for USER role");
            assertTrue(expression.contains("@lawyerVerificationChecker.isVerifiedLawyer()"), 
                "Should allow access for verified lawyers");
            assertTrue(expression.contains(" or "), 
                "Should use OR to allow either access path");
        }
    }

    @Nested
    @DisplayName("Expression Complexity Tests")
    class ExpressionComplexityTests {

        @Test
        @DisplayName("Should be more complex than simple role check")
        void shouldBeMoreComplexThanSimpleRoleCheck() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            assertTrue(expression.length() > "hasRole('USER')".length(), 
                "Should be more complex than simple role check");
            assertTrue(expression.contains("@"), "Should contain bean reference");
            assertTrue(expression.contains(" or "), "Should contain logical operator");
        }

        @Test
        @DisplayName("Should combine role-based and custom security")
        void shouldCombineRoleBasedAndCustomSecurity() {
            // Given
            PreAuthorize preAuthorize = RequireUserOrVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            // Verify it combines Spring Security role check with custom bean method
            assertTrue(expression.contains("hasRole"), "Should use Spring Security role check");
            assertTrue(expression.contains("@lawyerVerificationChecker"), "Should use custom bean");
            assertEquals(2, expression.split(" or ").length, 
                "Should have exactly 2 security conditions");
        }
    }

    // Test helper classes
    static class TestController {
        @RequireUserOrVerifiedLawyer
        public void userOrVerifiedLawyerMethod() {
            // Test method for annotation testing
        }
    }

    @RequireUserOrVerifiedLawyer
    static class TestMixedAccessController {
        // Test class for annotation testing
    }
} 