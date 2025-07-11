package com.javajedis.legalconnect.caseassets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.javajedis.legalconnect.caseassets.CaseAssetUtility.CaseAssetValidationResult;
import com.javajedis.legalconnect.casemanagement.Case;
import com.javajedis.legalconnect.casemanagement.CaseRepo;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.GetUserUtil;
import com.javajedis.legalconnect.lawyer.Lawyer;
import com.javajedis.legalconnect.user.User;
import com.javajedis.legalconnect.user.UserRepo;

@ExtendWith(MockitoExtension.class)
class CaseAssetUtilityTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private CaseRepo caseRepo;

    @Mock
    private User mockUser;

    @Mock
    private User mockClient;

    @Mock
    private User mockLawyerUser;

    @Mock
    private Lawyer mockLawyer;

    @Mock
    private Case mockCase;

    private UUID testUserId;
    private UUID testCaseId;
    private UUID testClientId;
    private UUID testLawyerId;
    private String testOperation;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCaseId = UUID.randomUUID();
        testClientId = UUID.randomUUID();
        testLawyerId = UUID.randomUUID();
        testOperation = "test operation";

        // Setup lenient stubs to avoid unnecessary stubbing errors
        lenient().when(mockUser.getId()).thenReturn(testUserId);
        lenient().when(mockUser.getEmail()).thenReturn("test@example.com");
        lenient().when(mockClient.getId()).thenReturn(testClientId);
        lenient().when(mockLawyerUser.getId()).thenReturn(testLawyerId);
        lenient().when(mockLawyer.getUser()).thenReturn(mockLawyerUser);
        lenient().when(mockCase.getId()).thenReturn(testCaseId);
        lenient().when(mockCase.getClient()).thenReturn(mockClient);
        lenient().when(mockCase.getLawyer()).thenReturn(mockLawyer);
    }

    @Test
    void testPrivateConstructor() {
        assertThrows(InvocationTargetException.class, () -> {
            Constructor<CaseAssetUtility> constructor = CaseAssetUtility.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void testValidateUserAndCaseAccess_UserNotAuthenticated() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            CaseAssetValidationResult<Object> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, testOperation, userRepo, caseRepo);

            assertTrue(result.hasError());
            assertNull(result.user());
            assertNull(result.caseEntity());
            assertNotNull(result.errorResponse());
            assertEquals(HttpStatus.UNAUTHORIZED, result.errorResponse().getStatusCode());
            assertEquals("User is not authenticated", result.errorResponse().getBody().getError().getMessage());
        }
    }

    @Test
    void testValidateUserAndCaseAccess_CaseNotFound() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(mockUser);
            when(caseRepo.findById(testCaseId)).thenReturn(Optional.empty());

            CaseAssetValidationResult<Object> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, testOperation, userRepo, caseRepo);

            assertTrue(result.hasError());
            assertEquals(mockUser, result.user());
            assertNull(result.caseEntity());
            assertNotNull(result.errorResponse());
            assertEquals(HttpStatus.NOT_FOUND, result.errorResponse().getStatusCode());
            assertEquals("Case not found", result.errorResponse().getBody().getError().getMessage());
        }
    }

    @Test
    void testValidateUserAndCaseAccess_UserAccessDenied() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(mockUser);
            when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(mockCase));

            CaseAssetValidationResult<Object> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, testOperation, userRepo, caseRepo);

            assertTrue(result.hasError());
            assertEquals(mockUser, result.user());
            assertEquals(mockCase, result.caseEntity());
            assertNotNull(result.errorResponse());
            assertEquals(HttpStatus.FORBIDDEN, result.errorResponse().getStatusCode());
            assertEquals("You don't have permission to access this case", result.errorResponse().getBody().getError().getMessage());
        }
    }

    @Test
    void testValidateUserAndCaseAccess_SuccessAsClient() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            when(mockUser.getId()).thenReturn(testClientId); // User is the client
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(mockUser);
            when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(mockCase));

            CaseAssetValidationResult<Object> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, testOperation, userRepo, caseRepo);

            assertFalse(result.hasError());
            assertEquals(mockUser, result.user());
            assertEquals(mockCase, result.caseEntity());
            assertNull(result.errorResponse());
        }
    }

    @Test
    void testValidateUserAndCaseAccess_SuccessAsLawyer() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            when(mockUser.getId()).thenReturn(testLawyerId); // User is the lawyer
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(mockUser);
            when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(mockCase));

            CaseAssetValidationResult<Object> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, testOperation, userRepo, caseRepo);

            assertFalse(result.hasError());
            assertEquals(mockUser, result.user());
            assertEquals(mockCase, result.caseEntity());
            assertNull(result.errorResponse());
        }
    }

    @Test
    void testIsAssetOwner_True() {
        UUID ownerId = UUID.randomUUID();
        UUID currentUserId = ownerId;

        boolean result = CaseAssetUtility.isAssetOwner(ownerId, currentUserId);

        assertTrue(result);
    }

    @Test
    void testIsAssetOwner_False() {
        UUID ownerId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();

        boolean result = CaseAssetUtility.isAssetOwner(ownerId, currentUserId);

        assertFalse(result);
    }

    @Test
    void testIsAssetOwner_NullOwnerId() {
        UUID currentUserId = UUID.randomUUID();

        boolean result = CaseAssetUtility.isAssetOwner(null, currentUserId);

        assertFalse(result);
    }

    @Test
    void testIsAssetOwner_NullCurrentUserId() {
        UUID ownerId = UUID.randomUUID();

        boolean result = CaseAssetUtility.isAssetOwner(ownerId, null);

        assertFalse(result);
    }

    @Test
    void testIsAssetOwner_BothNull() {
        boolean result = CaseAssetUtility.isAssetOwner(null, null);

        assertFalse(result);
    }

    @Test
    void testIsCaseLawyer_True() {
        boolean result = CaseAssetUtility.isCaseLawyer(mockCase, testLawyerId);

        assertTrue(result);
    }

    @Test
    void testIsCaseLawyer_False() {
        UUID differentUserId = UUID.randomUUID();

        boolean result = CaseAssetUtility.isCaseLawyer(mockCase, differentUserId);

        assertFalse(result);
    }

    @Test
    void testIsCaseLawyer_NullCase() {
        boolean result = CaseAssetUtility.isCaseLawyer(null, testLawyerId);

        assertFalse(result);
    }

    @Test
    void testIsCaseLawyer_NullLawyer() {
        when(mockCase.getLawyer()).thenReturn(null);

        boolean result = CaseAssetUtility.isCaseLawyer(mockCase, testLawyerId);

        assertFalse(result);
    }

    @Test
    void testIsCaseLawyer_NullLawyerUser() {
        when(mockLawyer.getUser()).thenReturn(null);

        boolean result = CaseAssetUtility.isCaseLawyer(mockCase, testLawyerId);

        assertFalse(result);
    }

    @Test
    void testIsCaseLawyer_NullUserId() {
        boolean result = CaseAssetUtility.isCaseLawyer(mockCase, null);

        assertFalse(result);
    }

    @Test
    void testIsCaseClient_True() {
        boolean result = CaseAssetUtility.isCaseClient(mockCase, testClientId);

        assertTrue(result);
    }

    @Test
    void testIsCaseClient_False() {
        UUID differentUserId = UUID.randomUUID();

        boolean result = CaseAssetUtility.isCaseClient(mockCase, differentUserId);

        assertFalse(result);
    }

    @Test
    void testIsCaseClient_NullCase() {
        boolean result = CaseAssetUtility.isCaseClient(null, testClientId);

        assertFalse(result);
    }

    @Test
    void testIsCaseClient_NullClient() {
        when(mockCase.getClient()).thenReturn(null);

        boolean result = CaseAssetUtility.isCaseClient(mockCase, testClientId);

        assertFalse(result);
    }

    @Test
    void testIsCaseClient_NullUserId() {
        boolean result = CaseAssetUtility.isCaseClient(mockCase, null);

        assertFalse(result);
    }

    @Test
    void testCaseAssetValidationResult_HasError_True() {
        ResponseEntity<ApiResponse<Object>> errorResponse = ApiResponse.error("Not found", HttpStatus.NOT_FOUND);

        CaseAssetValidationResult<Object> result = new CaseAssetValidationResult<>(
            mockUser, mockCase, errorResponse);

        assertTrue(result.hasError());
        assertEquals(mockUser, result.user());
        assertEquals(mockCase, result.caseEntity());
        assertEquals(errorResponse, result.errorResponse());
    }

    @Test
    void testCaseAssetValidationResult_HasError_False() {
        CaseAssetValidationResult<Object> result = new CaseAssetValidationResult<>(
            mockUser, mockCase, null);

        assertFalse(result.hasError());
        assertEquals(mockUser, result.user());
        assertEquals(mockCase, result.caseEntity());
        assertNull(result.errorResponse());
    }

    @Test
    void testCaseAssetValidationResult_AllNull() {
        CaseAssetValidationResult<Object> result = new CaseAssetValidationResult<>(
            null, null, null);

        assertFalse(result.hasError());
        assertNull(result.user());
        assertNull(result.caseEntity());
        assertNull(result.errorResponse());
    }

    @Test
    void testValidateUserAndCaseAccess_GenericTypeParameter() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            when(mockUser.getId()).thenReturn(testClientId);
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(mockUser);
            when(caseRepo.findById(testCaseId)).thenReturn(Optional.of(mockCase));

            CaseAssetValidationResult<String> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, testOperation, userRepo, caseRepo);

            assertFalse(result.hasError());
            assertEquals(mockUser, result.user());
            assertEquals(mockCase, result.caseEntity());
            assertNull(result.errorResponse());
        }
    }

    @Test
    void testValidateUserAndCaseAccess_EdgeCaseEmptyOperation() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            CaseAssetValidationResult<Object> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, "", userRepo, caseRepo);

            assertTrue(result.hasError());
            assertEquals(HttpStatus.UNAUTHORIZED, result.errorResponse().getStatusCode());
        }
    }

    @Test
    void testValidateUserAndCaseAccess_EdgeCaseNullOperation() {
        try (MockedStatic<GetUserUtil> mockedGetUserUtil = mockStatic(GetUserUtil.class)) {
            mockedGetUserUtil.when(() -> GetUserUtil.getAuthenticatedUser(userRepo)).thenReturn(null);

            CaseAssetValidationResult<Object> result = CaseAssetUtility.validateUserAndCaseAccess(
                testCaseId, null, userRepo, caseRepo);

            assertTrue(result.hasError());
            assertEquals(HttpStatus.UNAUTHORIZED, result.errorResponse().getStatusCode());
        }
    }
} 