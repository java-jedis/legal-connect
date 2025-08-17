package com.javajedis.legalconnect.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Annotation for endpoints accessible by regular users or verified lawyers.
 * 
 * This annotation allows access to:
 * - Users with USER role (regular authenticated users)
 * - Users with LAWYER role who are also verified through LawyerVerificationChecker
 * 
 * This is useful for endpoints where both clients and lawyers need access,
 * but lawyers must be verified to participate.
 * 
 * The LawyerVerificationChecker internally validates both the LAWYER role and 
 * verification status, so this expression safely evaluates each condition.
 * 
 * Usage:
 * - On method: Only that specific method allows user or verified lawyer access
 * - On class: All methods in the class allow user or verified lawyer access
 * 
 * @see com.javajedis.legalconnect.common.security.LawyerVerificationChecker
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('USER') or @lawyerVerificationChecker.isVerifiedLawyer()")
public @interface RequireUserOrVerifiedLawyer {
} 