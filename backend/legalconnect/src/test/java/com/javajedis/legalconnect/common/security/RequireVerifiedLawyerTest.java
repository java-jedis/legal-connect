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
 * Unit tests for RequireVerifiedLawyer annotation.
 * Verifies annotation configuration, metadata, and expected behavior.
 */
@DisplayName("RequireVerifiedLawyer Annotation Tests")
class RequireVerifiedLawyerTest {

    @Nested
    @DisplayName("Annotation Configuration Tests")
    class AnnotationConfigurationTests {

        @Test
        @DisplayName("Should have correct target types")
        void shouldHaveCorrectTargetTypes() {
            // Given
            Target target = RequireVerifiedLawyer.class.getAnnotation(Target.class);

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
            Retention retention = RequireVerifiedLawyer.class.getAnnotation(Retention.class);

            // Then
            assertNotNull(retention, "Retention annotation should be present");
            assertEquals(RetentionPolicy.RUNTIME, retention.value(), 
                "Should have RUNTIME retention for Spring Security to process");
        }

        @Test
        @DisplayName("Should have correct PreAuthorize expression")
        void shouldHaveCorrectPreAuthorizeExpression() {
            // Given
            PreAuthorize preAuthorize = RequireVerifiedLawyer.class.getAnnotation(PreAuthorize.class);

            // Then
            assertNotNull(preAuthorize, "PreAuthorize annotation should be present");
            assertEquals("@lawyerVerificationChecker.isVerifiedLawyer()", preAuthorize.value(), 
                "Should use lawyerVerificationChecker bean for verification");
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
            Method testMethod = TestController.class.getMethod("verifiedLawyerOnlyMethod");

            // When
            RequireVerifiedLawyer annotation = testMethod.getAnnotation(RequireVerifiedLawyer.class);

            // Then
            assertNotNull(annotation, "Annotation should be present on method");
        }

        @Test
        @DisplayName("Should be applicable to classes")
        void shouldBeApplicableToClasses() {
            // Given
            Class<TestVerifiedLawyerController> testClass = TestVerifiedLawyerController.class;

            // When
            RequireVerifiedLawyer annotation = testClass.getAnnotation(RequireVerifiedLawyer.class);

            // Then
            assertNotNull(annotation, "Annotation should be present on class");
        }

        @Test
        @DisplayName("Should inherit PreAuthorize expression when applied")
        void shouldInheritPreAuthorizeExpressionWhenApplied() throws NoSuchMethodException {
            // Given
            Method testMethod = TestController.class.getMethod("verifiedLawyerOnlyMethod");

            // When
            RequireVerifiedLawyer requireVerifiedLawyer = testMethod.getAnnotation(RequireVerifiedLawyer.class);
            PreAuthorize preAuthorize = RequireVerifiedLawyer.class.getAnnotation(PreAuthorize.class);

            // Then
            assertNotNull(requireVerifiedLawyer, "RequireVerifiedLawyer should be present");
            assertNotNull(preAuthorize, "PreAuthorize should be inherited");
            assertEquals("@lawyerVerificationChecker.isVerifiedLawyer()", preAuthorize.value(),
                "Should contain correct security expression");
        }
    }

    @Nested
    @DisplayName("Security Expression Validation Tests")
    class SecurityExpressionValidationTests {

        @Test
        @DisplayName("Should reference correct Spring bean")
        void shouldReferenceCorrectSpringBean() {
            // Given
            PreAuthorize preAuthorize = RequireVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            assertTrue(expression.contains("@lawyerVerificationChecker"), 
                "Should reference lawyerVerificationChecker bean");
            assertTrue(expression.contains("isVerifiedLawyer()"), 
                "Should call isVerifiedLawyer method");
            assertEquals("@lawyerVerificationChecker.isVerifiedLawyer()", expression,
                "Should have exact expected expression");
        }

        @Test
        @DisplayName("Should be a simple single-condition expression")
        void shouldBeSimpleSingleConditionExpression() {
            // Given
            PreAuthorize preAuthorize = RequireVerifiedLawyer.class.getAnnotation(PreAuthorize.class);
            String expression = preAuthorize.value();

            // Then
            // Verify it's a simple expression without complex logic
            assertTrue(!expression.contains(" and "), "Should not contain AND logic");
            assertTrue(!expression.contains(" or "), "Should not contain OR logic");
            assertTrue(!expression.contains("hasRole"), "Should not contain role checks");
            assertTrue(expression.startsWith("@"), "Should be a bean method call");
        }
    }

    // Test helper classes
    static class TestController {
        @RequireVerifiedLawyer
        public void verifiedLawyerOnlyMethod() {
            // Test method for annotation testing
        }
    }

    @RequireVerifiedLawyer
    static class TestVerifiedLawyerController {
        // Test class for annotation testing
    }
} 