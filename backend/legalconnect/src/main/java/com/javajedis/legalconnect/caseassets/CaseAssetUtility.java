package com.javajedis.legalconnect.caseassets;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for case asset operations including validation and common functionality.
 * Provides reusable methods for notes, documents, and other case assets.
 */
@Slf4j
public class CaseAssetUtility {
    
    private static final String NOT_AUTHENTICATED_MSG = "User is not authenticated";
    private static final String CASE_NOT_FOUND_LOG = "Case not found with ID: {}";
    private static final String CASE_NOT_FOUND_MSG = "Case not found";
    private static final String CASE_ACCESS_DENIED_MSG = "You don't have permission to access this case";

    // Private constructor to prevent instantiation
    private CaseAssetUtility() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates user authentication and case access for case asset operations.
     * Generic method that can be used for notes, documents, and other case assets.
     *
     * @param <T> the type of the response DTO
     * @param caseId the ID of the case to validate access for
     * @param operation the operation being performed (for logging)
     * @param userRepo the user repository
     * @param caseRepo the case repository
     * @return CaseAssetValidationResult containing validation results
     */
    public static <T> CaseAssetValidationResult<T> validateUserAndCaseAccess(
            UUID caseId, 
            String operation, 
            UserRepo userRepo, 
            CaseRepo caseRepo) {
        
        User user = GetUserUtil.getAuthenticatedUser(userRepo);

        if (user == null) {
            log.warn("Unauthorized {} attempt", operation);
            return new CaseAssetValidationResult<>(null, null, 
                ApiResponse.error(NOT_AUTHENTICATED_MSG, HttpStatus.UNAUTHORIZED));
        }

        Case caseEntity = caseRepo.findById(caseId).orElse(null);

        if (caseEntity == null) {
            log.warn(CASE_NOT_FOUND_LOG, caseId);
            return new CaseAssetValidationResult<>(user, null,
                ApiResponse.error(CASE_NOT_FOUND_MSG, HttpStatus.NOT_FOUND));
        }

        boolean hasAccess = caseEntity.getClient().getId().equals(user.getId())
                || caseEntity.getLawyer().getUser().getId().equals(user.getId());

        if (!hasAccess) {
            log.warn("User {} attempted to {} for case {} without permission", user.getEmail(), operation, caseId);
            return new CaseAssetValidationResult<>(user, caseEntity,
                ApiResponse.error(CASE_ACCESS_DENIED_MSG, HttpStatus.FORBIDDEN));
        }

        return new CaseAssetValidationResult<>(user, caseEntity, null);
    }

    /**
     * Checks if a user is the owner of a case asset (created by them).
     * This can be used for additional authorization checks on update/delete operations.
     *
     * @param assetOwnerId the ID of the user who owns the asset
     * @param currentUserId the ID of the current authenticated user
     * @return true if the current user owns the asset, false otherwise
     */
    public static boolean isAssetOwner(UUID assetOwnerId, UUID currentUserId) {
        return assetOwnerId != null && assetOwnerId.equals(currentUserId);
    }

    /**
     * Checks if a user is the lawyer for a case.
     * This can be used for lawyer-specific operations.
     *
     * @param caseEntity the case entity
     * @param userId the user ID to check
     * @return true if the user is the lawyer for the case, false otherwise
     */
    public static boolean isCaseLawyer(Case caseEntity, UUID userId) {
        return caseEntity != null && 
               caseEntity.getLawyer() != null && 
               caseEntity.getLawyer().getUser() != null &&
               caseEntity.getLawyer().getUser().getId().equals(userId);
    }

    /**
     * Checks if a user is the client for a case.
     * This can be used for client-specific operations.
     *
     * @param caseEntity the case entity
     * @param userId the user ID to check
     * @return true if the user is the client for the case, false otherwise
     */
    public static boolean isCaseClient(Case caseEntity, UUID userId) {
        return caseEntity != null && 
               caseEntity.getClient() != null && 
               caseEntity.getClient().getId().equals(userId);
    }

    /**
     * Generic helper record to hold case asset validation results.
     * Can be used for notes, documents, and other case assets.
     *
     * @param <T> the type of the response DTO
     * @param user the authenticated user
     * @param caseEntity the case entity
     * @param errorResponse the error response if validation failed
     */
    public record CaseAssetValidationResult<T>(
            User user,
            Case caseEntity,
            ResponseEntity<ApiResponse<T>> errorResponse
    ) {
        public boolean hasError() {
            return errorResponse != null;
        }
    }
} 