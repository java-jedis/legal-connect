package com.javajedis.legalconnect.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Annotation to require verified lawyer access.
 * Ensures the user has LAWYER role and is verified through LawyerVerificationChecker.
 * 
 * This annotation can be applied to methods or classes (controllers) to enforce
 * that only verified lawyers can access the annotated resources.
 * 
 * The LawyerVerificationChecker internally validates both the LAWYER role and 
 * verification status, providing a single point of truth for lawyer verification.
 * 
 * Usage:
 * - On method: Only that specific method requires verified lawyer access
 * - On class: All methods in the class require verified lawyer access
 * 
 * @see com.javajedis.legalconnect.common.security.LawyerVerificationChecker
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@lawyerVerificationChecker.isVerifiedLawyer()")
public @interface RequireVerifiedLawyer {
} 