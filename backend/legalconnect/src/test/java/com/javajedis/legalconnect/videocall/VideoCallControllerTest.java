package com.javajedis.legalconnect.videocall;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.utility.EmailVerificationFilter;
import com.javajedis.legalconnect.common.utility.JWTFilter;
import com.javajedis.legalconnect.videocall.dto.MeetingResponseDTO;
import com.javajedis.legalconnect.videocall.dto.MeetingTokenDTO;
import com.javajedis.legalconnect.videocall.dto.ScheduleMeetingDTO;
import com.javajedis.legalconnect.videocall.dto.UpdateMeetingDTO;

@WebMvcTest(controllers = VideoCallController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {EmailVerificationFilter.class, JWTFilter.class}))
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
@Import(VideoCallControllerTest.TestConfig.class)
@DisplayName("VideoCallController Tests")
class VideoCallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VideoCallService videoCallService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public VideoCallService videoCallService() {
            return mock(VideoCallService.class);
        }
    }

    private ScheduleMeetingDTO scheduleMeetingDTO;
    private UpdateMeetingDTO updateMeetingDTO;
    private MeetingResponseDTO meetingResponseDTO;
    private MeetingTokenDTO meetingTokenDTO;
    private UUID testMeetingId;
    private UUID testClientId;
    private UUID testLawyerId;
    private UUID testPaymentId;
    private OffsetDateTime testStartTime;
    private OffsetDateTime testEndTime;

    @BeforeEach
    void setUp() {
        // Reset the mock before each test to ensure clean state
        reset(videoCallService);
        
        testMeetingId = UUID.randomUUID();
        testClientId = UUID.randomUUID();
        testLawyerId = UUID.randomUUID();
        testPaymentId = UUID.randomUUID();
        testStartTime = OffsetDateTime.now().plusHours(1);
        testEndTime = testStartTime.plusHours(1);

        // Setup ScheduleMeetingDTO
        scheduleMeetingDTO = new ScheduleMeetingDTO();
        scheduleMeetingDTO.setEmail("client@example.com");
        scheduleMeetingDTO.setTitle("Legal Consultation");
        scheduleMeetingDTO.setStartDateTime(testStartTime);
        scheduleMeetingDTO.setEndDateTime(testEndTime);

        // Setup UpdateMeetingDTO
        updateMeetingDTO = new UpdateMeetingDTO();
        updateMeetingDTO.setMeetingId(testMeetingId);
        updateMeetingDTO.setStartDateTime(testStartTime.plusDays(1));
        updateMeetingDTO.setEndDateTime(testEndTime.plusDays(1));

        // Setup MeetingResponseDTO with nested classes
        MeetingResponseDTO.UserInfo clientInfo = new MeetingResponseDTO.UserInfo();
        clientInfo.setId(testClientId);
        clientInfo.setFirstName("John");
        clientInfo.setLastName("Doe");
        clientInfo.setEmail("client@example.com");

        MeetingResponseDTO.UserInfo lawyerInfo = new MeetingResponseDTO.UserInfo();
        lawyerInfo.setId(testLawyerId);
        lawyerInfo.setFirstName("Jane");
        lawyerInfo.setLastName("Smith");
        lawyerInfo.setEmail("lawyer@example.com");

        MeetingResponseDTO.PaymentInfo paymentInfo = new MeetingResponseDTO.PaymentInfo();
        paymentInfo.setId(testPaymentId);
        paymentInfo.setAmount(new BigDecimal("100.00"));
        paymentInfo.setStatus("PENDING");

        meetingResponseDTO = new MeetingResponseDTO();
        meetingResponseDTO.setId(testMeetingId);
        meetingResponseDTO.setRoomName("room_" + testMeetingId);
        meetingResponseDTO.setClient(clientInfo);
        meetingResponseDTO.setLawyer(lawyerInfo);
        meetingResponseDTO.setStartTimestamp(testStartTime);
        meetingResponseDTO.setEndTimestamp(testEndTime);
        meetingResponseDTO.setPaid(false);
        meetingResponseDTO.setPayment(paymentInfo);

        // Setup MeetingTokenDTO
        meetingTokenDTO = new MeetingTokenDTO();
        meetingTokenDTO.setMeetingId(testMeetingId);
        meetingTokenDTO.setRoomName("room_" + testMeetingId);
        meetingTokenDTO.setJwtToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");
    }
   
 // Task 3.2: Schedule meeting endpoint tests
    @Test
    @DisplayName("Should schedule meeting successfully with valid data")
    void scheduleMeeting_ValidData_Success() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> responseEntity = 
            ApiResponse.success(meetingResponseDTO, HttpStatus.CREATED, "Meeting scheduled successfully");
        when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(testMeetingId.toString()))
                .andExpect(jsonPath("$.data.client.email").value("client@example.com"))
                .andExpect(jsonPath("$.data.lawyer.email").value("lawyer@example.com"))
                .andExpect(jsonPath("$.message").value("Meeting scheduled successfully"));
    }

    @Test
    @DisplayName("Should return bad request when client email is null")
    void scheduleMeeting_NullClientEmail_BadRequest() throws Exception {
        scheduleMeetingDTO.setEmail(null);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept empty client email since validation only checks for null")
    void scheduleMeeting_EmptyClientEmail_Success() throws Exception {
        scheduleMeetingDTO.setEmail("");
        
        ResponseEntity<ApiResponse<MeetingResponseDTO>> responseEntity = 
            ApiResponse.success(meetingResponseDTO, HttpStatus.CREATED, "Meeting scheduled successfully");
        when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Meeting scheduled successfully"));
    }

    @Test
    @DisplayName("Should return bad request when meeting title is null")
    void scheduleMeeting_NullTitle_BadRequest() throws Exception {
        scheduleMeetingDTO.setTitle(null);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should accept empty meeting title since validation only checks for null")
    void scheduleMeeting_EmptyTitle_Success() throws Exception {
        scheduleMeetingDTO.setTitle("");
        
        ResponseEntity<ApiResponse<MeetingResponseDTO>> responseEntity = 
            ApiResponse.success(meetingResponseDTO, HttpStatus.CREATED, "Meeting scheduled successfully");
        when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Meeting scheduled successfully"));
    }

    @Test
    @DisplayName("Should return bad request when start datetime is null")
    void scheduleMeeting_NullStartDateTime_BadRequest() throws Exception {
        scheduleMeetingDTO.setStartDateTime(null);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when end datetime is null")
    void scheduleMeeting_NullEndDateTime_BadRequest() throws Exception {
        scheduleMeetingDTO.setEndDateTime(null);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when request body is empty")
    void scheduleMeeting_EmptyRequestBody_BadRequest() throws Exception {
        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return internal server error when content type is not JSON")
    void scheduleMeeting_InvalidContentType_InternalServerError() throws Exception {
        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return unauthorized when service returns unauthorized")
    void scheduleMeeting_Unauthorized_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not verified lawyer")
    void scheduleMeeting_NotVerifiedLawyer_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("Only verified lawyers can schedule meetings", HttpStatus.FORBIDDEN);
        when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Only verified lawyers can schedule meetings"));
    }

    @Test
    @DisplayName("Should return not found when client not found")
    void scheduleMeeting_ClientNotFound_NotFound() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("Client not found with email: " + scheduleMeetingDTO.getEmail(), HttpStatus.NOT_FOUND);
        when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Client not found with email: client@example.com"));
    }

    // Parameterized test for invalid meeting times validation
    static Stream<Arguments> invalidMeetingTimesEndpoints() {
        return Stream.of(
            Arguments.of("schedule", "post", "/videocalls/"),
            Arguments.of("update", "put", "/videocalls/")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidMeetingTimesEndpoints")
    @DisplayName("Should return bad request when meeting times are invalid")
    void invalidMeetingTimes_BadRequest(String operation, String method, String endpoint) throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("End time must be after start time", HttpStatus.BAD_REQUEST);
        
        switch (operation) {
            case "schedule":
                when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class))).thenReturn(errorResponse);
                mockMvc.perform(post(endpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error.message").value("End time must be after start time"));
                break;
            case "update":
                when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class))).thenReturn(errorResponse);
                mockMvc.perform(put(endpoint)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.error.message").value("End time must be after start time"));
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void scheduleMeeting_ServiceException_InternalServerError() throws Exception {
        when(videoCallService.scheduleMeeting(any(ScheduleMeetingDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scheduleMeetingDTO)))
                .andExpect(status().isInternalServerError());
    }
 
   // Task 3.3: Update meeting endpoint tests
    @Test
    @DisplayName("Should update meeting successfully with valid data")
    void updateMeeting_ValidData_Success() throws Exception {
        meetingResponseDTO.setStartTimestamp(updateMeetingDTO.getStartDateTime());
        meetingResponseDTO.setEndTimestamp(updateMeetingDTO.getEndDateTime());
        
        ResponseEntity<ApiResponse<MeetingResponseDTO>> responseEntity = 
            ApiResponse.success(meetingResponseDTO, HttpStatus.OK, "Meeting updated successfully");
        when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class))).thenReturn(responseEntity);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testMeetingId.toString()))
                .andExpect(jsonPath("$.message").value("Meeting updated successfully"));
    }

    @Test
    @DisplayName("Should return bad request when meeting ID is null")
    void updateMeeting_NullMeetingId_BadRequest() throws Exception {
        updateMeetingDTO.setMeetingId(null);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when start datetime is null")
    void updateMeeting_NullStartDateTime_BadRequest() throws Exception {
        updateMeetingDTO.setStartDateTime(null);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when end datetime is null")
    void updateMeeting_NullEndDateTime_BadRequest() throws Exception {
        updateMeetingDTO.setEndDateTime(null);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return bad request when request body is empty")
    void updateMeeting_EmptyRequestBody_BadRequest() throws Exception {
        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return internal server error when content type is not JSON")
    void updateMeeting_InvalidContentType_InternalServerError() throws Exception {
        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return unauthorized when service returns unauthorized")
    void updateMeeting_Unauthorized_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not verified lawyer")
    void updateMeeting_NotVerifiedLawyer_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("Only verified lawyers can update meetings", HttpStatus.FORBIDDEN);
        when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Only verified lawyers can update meetings"));
    }

    @Test
    @DisplayName("Should return not found when meeting not found")
    void updateMeeting_MeetingNotFound_NotFound() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("Meeting not found with ID: " + testMeetingId, HttpStatus.NOT_FOUND);
        when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Meeting not found with ID: " + testMeetingId));
    }

    @Test
    @DisplayName("Should return bad request when meeting is already paid")
    void updateMeeting_MeetingAlreadyPaid_BadRequest() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("Cannot update paid meeting", HttpStatus.BAD_REQUEST);
        when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Cannot update paid meeting"));
    }

    @Test
    @DisplayName("Should return bad request when meeting has already started")
    void updateMeeting_MeetingAlreadyStarted_BadRequest() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("Cannot update meeting that has already started", HttpStatus.BAD_REQUEST);
        when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class))).thenReturn(errorResponse);

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Cannot update meeting that has already started"));
    }



    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void updateMeeting_ServiceException_InternalServerError() throws Exception {
        when(videoCallService.updateMeeting(any(UpdateMeetingDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(put("/videocalls/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMeetingDTO)))
                .andExpect(status().isInternalServerError());
    } 
   // Task 3.4: Delete meeting endpoint tests
    @Test
    @DisplayName("Should delete meeting successfully with valid ID")
    void deleteMeeting_ValidId_Success() throws Exception {
        ResponseEntity<ApiResponse<String>> responseEntity = 
            ApiResponse.success("Meeting deleted successfully", HttpStatus.OK, "Meeting deleted");
        when(videoCallService.deleteMeeting(testMeetingId)).thenReturn(responseEntity);

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Meeting deleted successfully"))
                .andExpect(jsonPath("$.message").value("Meeting deleted"));
    }

    @Test
    @DisplayName("Should return not found when meeting does not exist")
    void deleteMeeting_NonExistentId_NotFound() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("Meeting not found with ID: " + testMeetingId, HttpStatus.NOT_FOUND);
        when(videoCallService.deleteMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Meeting not found with ID: " + testMeetingId));
    }

    // Parameterized test for invalid UUID format across different endpoints
    static Stream<Arguments> invalidUuidEndpoints() {
        return Stream.of(
            Arguments.of("delete", "/videocalls/{meetingId}"),
            Arguments.of("get", "/videocalls/{meetingId}"),
            Arguments.of("get", "/videocalls/{meetingId}/token")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUuidEndpoints")
    @DisplayName("Should return internal server error when meeting ID is invalid UUID format")
    void invalidUuidFormat_InternalServerError(String method, String endpoint) throws Exception {
        switch (method) {
            case "delete":
                mockMvc.perform(delete(endpoint, "invalid-uuid"))
                        .andExpect(status().isInternalServerError());
                break;
            case "get":
                mockMvc.perform(get(endpoint, "invalid-uuid"))
                        .andExpect(status().isInternalServerError());
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    @Test
    @DisplayName("Should return unauthorized when service returns unauthorized")
    void deleteMeeting_Unauthorized_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(videoCallService.deleteMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not verified lawyer")
    void deleteMeeting_NotVerifiedLawyer_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("Only verified lawyers can delete meetings", HttpStatus.FORBIDDEN);
        when(videoCallService.deleteMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("Only verified lawyers can delete meetings"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not authorized to delete meeting")
    void deleteMeeting_NotAuthorized_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("You are not authorized to delete this meeting", HttpStatus.FORBIDDEN);
        when(videoCallService.deleteMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You are not authorized to delete this meeting"));
    }

    @Test
    @DisplayName("Should return bad request when meeting is already paid")
    void deleteMeeting_MeetingAlreadyPaid_BadRequest() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("Cannot delete paid meeting", HttpStatus.BAD_REQUEST);
        when(videoCallService.deleteMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Cannot delete paid meeting"));
    }

    @Test
    @DisplayName("Should return bad request when meeting has already started")
    void deleteMeeting_MeetingAlreadyStarted_BadRequest() throws Exception {
        ResponseEntity<ApiResponse<String>> errorResponse = 
            ApiResponse.error("Cannot delete meeting that has already started", HttpStatus.BAD_REQUEST);
        when(videoCallService.deleteMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Cannot delete meeting that has already started"));
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void deleteMeeting_ServiceException_InternalServerError() throws Exception {
        when(videoCallService.deleteMeeting(testMeetingId))
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(delete("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isInternalServerError());
    }

    // Task 3.5: Get meeting endpoint tests
    @Test
    @DisplayName("Should retrieve meeting successfully with valid ID")
    void getMeeting_ValidId_Success() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> responseEntity = 
            ApiResponse.success(meetingResponseDTO, HttpStatus.OK, "Meeting retrieved successfully");
        when(videoCallService.getMeeting(testMeetingId)).thenReturn(responseEntity);

        mockMvc.perform(get("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testMeetingId.toString()))
                .andExpect(jsonPath("$.data.client.email").value("client@example.com"))
                .andExpect(jsonPath("$.data.lawyer.email").value("lawyer@example.com"))
                .andExpect(jsonPath("$.data.roomName").value("room_" + testMeetingId))
                .andExpect(jsonPath("$.data.paid").value(false))
                .andExpect(jsonPath("$.data.payment.amount").value(100.00))
                .andExpect(jsonPath("$.message").value("Meeting retrieved successfully"));
    }

    @Test
    @DisplayName("Should return not found when meeting does not exist")
    void getMeeting_NonExistentId_NotFound() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("Meeting not found with ID: " + testMeetingId, HttpStatus.NOT_FOUND);
        when(videoCallService.getMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Meeting not found with ID: " + testMeetingId));
    }



    @Test
    @DisplayName("Should return unauthorized when service returns unauthorized")
    void getMeeting_Unauthorized_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(videoCallService.getMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should return forbidden when user is not authorized to view meeting")
    void getMeeting_NotAuthorized_Forbidden() throws Exception {
        ResponseEntity<ApiResponse<MeetingResponseDTO>> errorResponse = 
            ApiResponse.error("You are not authorized to view this meeting", HttpStatus.FORBIDDEN);
        when(videoCallService.getMeeting(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message").value("You are not authorized to view this meeting"));
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void getMeeting_ServiceException_InternalServerError() throws Exception {
        when(videoCallService.getMeeting(testMeetingId))
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/videocalls/{meetingId}", testMeetingId))
                .andExpect(status().isInternalServerError());
    }

    // Task 3.6: Get meetings endpoint tests
    @Test
    @DisplayName("Should retrieve all meetings successfully for authenticated user")
    void getMeetings_AuthenticatedUser_Success() throws Exception {
        List<MeetingResponseDTO> meetings = List.of(meetingResponseDTO);
        ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> responseEntity = 
            ApiResponse.success(meetings, HttpStatus.OK, "Meetings retrieved successfully");
        when(videoCallService.getMeetings()).thenReturn(responseEntity);

        mockMvc.perform(get("/videocalls/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testMeetingId.toString()))
                .andExpect(jsonPath("$.data[0].client.email").value("client@example.com"))
                .andExpect(jsonPath("$.data[0].lawyer.email").value("lawyer@example.com"))
                .andExpect(jsonPath("$.message").value("Meetings retrieved successfully"));
    }

    @Test
    @DisplayName("Should return empty list when no meetings found")
    void getMeetings_NoMeetings_EmptyList() throws Exception {
        List<MeetingResponseDTO> emptyMeetings = List.of();
        ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> responseEntity = 
            ApiResponse.success(emptyMeetings, HttpStatus.OK, "Meetings retrieved successfully");
        when(videoCallService.getMeetings()).thenReturn(responseEntity);

        mockMvc.perform(get("/videocalls/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.message").value("Meetings retrieved successfully"));
    }

    @Test
    @DisplayName("Should return multiple meetings when user has multiple meetings")
    void getMeetings_MultipleMeetings_Success() throws Exception {
        // Create second meeting
        MeetingResponseDTO secondMeeting = new MeetingResponseDTO();
        secondMeeting.setId(UUID.randomUUID());
        secondMeeting.setRoomName("room_" + secondMeeting.getId());
        secondMeeting.setClient(meetingResponseDTO.getClient());
        secondMeeting.setLawyer(meetingResponseDTO.getLawyer());
        secondMeeting.setStartTimestamp(testStartTime.plusDays(1));
        secondMeeting.setEndTimestamp(testEndTime.plusDays(1));
        secondMeeting.setPaid(true);
        secondMeeting.setPayment(meetingResponseDTO.getPayment());

        List<MeetingResponseDTO> meetings = List.of(meetingResponseDTO, secondMeeting);
        ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> responseEntity = 
            ApiResponse.success(meetings, HttpStatus.OK, "Meetings retrieved successfully");
        when(videoCallService.getMeetings()).thenReturn(responseEntity);

        mockMvc.perform(get("/videocalls/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data[0].id").value(testMeetingId.toString()))
                .andExpect(jsonPath("$.data[1].id").value(secondMeeting.getId().toString()))
                .andExpect(jsonPath("$.data[0].paid").value(false))
                .andExpect(jsonPath("$.data[1].paid").value(true))
                .andExpect(jsonPath("$.message").value("Meetings retrieved successfully"));
    }

    @Test
    @DisplayName("Should return unauthorized when service returns unauthorized")
    void getMeetings_Unauthorized_Unauthorized() throws Exception {
        ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> errorResponse = 
            ApiResponse.error("User is not authenticated", HttpStatus.UNAUTHORIZED);
        when(videoCallService.getMeetings()).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.message").value("User is not authenticated"));
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void getMeetings_ServiceException_InternalServerError() throws Exception {
        when(videoCallService.getMeetings())
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/videocalls/"))
                .andExpect(status().isInternalServerError());
    }

    // Task 3.7: Generate meeting token endpoint tests
    @Test
    @DisplayName("Should generate meeting token successfully with valid meeting ID")
    void generateMeetingToken_ValidMeetingId_Success() throws Exception {
        ResponseEntity<ApiResponse<MeetingTokenDTO>> responseEntity = 
            ApiResponse.success(meetingTokenDTO, HttpStatus.OK, "Meeting token generated successfully");
        when(videoCallService.generateMeetingToken(testMeetingId)).thenReturn(responseEntity);

        mockMvc.perform(get("/videocalls/{meetingId}/token", testMeetingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingId").value(testMeetingId.toString()))
                .andExpect(jsonPath("$.data.roomName").value("room_" + testMeetingId))
                .andExpect(jsonPath("$.data.jwtToken").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token"))
                .andExpect(jsonPath("$.message").value("Meeting token generated successfully"));
    }

    @Test
    @DisplayName("Should return not found when meeting does not exist")
    void generateMeetingToken_NonExistentMeeting_NotFound() throws Exception {
        ResponseEntity<ApiResponse<MeetingTokenDTO>> errorResponse = 
            ApiResponse.error("Meeting not found with ID: " + testMeetingId, HttpStatus.NOT_FOUND);
        when(videoCallService.generateMeetingToken(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}/token", testMeetingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.message").value("Meeting not found with ID: " + testMeetingId));
    }



    // Parameterized test for generate meeting token error scenarios
    static Stream<Arguments> generateMeetingTokenErrorScenarios() {
        return Stream.of(
            Arguments.of(
                "User is not authenticated", 
                HttpStatus.UNAUTHORIZED, 
                "User is not authenticated"
            ),
            Arguments.of(
                "You are not authorized to access this meeting", 
                HttpStatus.FORBIDDEN, 
                "You are not authorized to access this meeting"
            ),
            Arguments.of(
                "Meeting must be paid before generating token", 
                HttpStatus.BAD_REQUEST, 
                "Meeting must be paid before generating token"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("generateMeetingTokenErrorScenarios")
    @DisplayName("Should handle generate meeting token error scenarios")
    void generateMeetingToken_ErrorScenarios(String errorMessage, HttpStatus expectedStatus, String expectedErrorMessage) throws Exception {
        ResponseEntity<ApiResponse<MeetingTokenDTO>> errorResponse = 
            ApiResponse.error(errorMessage, expectedStatus);
        when(videoCallService.generateMeetingToken(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}/token", testMeetingId))
                .andExpect(status().is(expectedStatus.value()))
                .andExpect(jsonPath("$.error.message").value(expectedErrorMessage));
    }

    @Test
    @DisplayName("Should return bad request when meeting has not started yet")
    void generateMeetingToken_MeetingNotStarted_BadRequest() throws Exception {
        ResponseEntity<ApiResponse<MeetingTokenDTO>> errorResponse = 
            ApiResponse.error("Meeting has not started yet", HttpStatus.BAD_REQUEST);
        when(videoCallService.generateMeetingToken(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}/token", testMeetingId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Meeting has not started yet"));
    }

    @Test
    @DisplayName("Should return bad request when meeting has already ended")
    void generateMeetingToken_MeetingEnded_BadRequest() throws Exception {
        ResponseEntity<ApiResponse<MeetingTokenDTO>> errorResponse = 
            ApiResponse.error("Meeting has already ended", HttpStatus.BAD_REQUEST);
        when(videoCallService.generateMeetingToken(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}/token", testMeetingId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Meeting has already ended"));
    }

    @Test
    @DisplayName("Should return internal server error when JWT generation fails")
    void generateMeetingToken_JwtGenerationFails_InternalServerError() throws Exception {
        ResponseEntity<ApiResponse<MeetingTokenDTO>> errorResponse = 
            ApiResponse.error("Failed to generate JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        when(videoCallService.generateMeetingToken(testMeetingId)).thenReturn(errorResponse);

        mockMvc.perform(get("/videocalls/{meetingId}/token", testMeetingId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.message").value("Failed to generate JWT token"));
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void generateMeetingToken_ServiceException_InternalServerError() throws Exception {
        when(videoCallService.generateMeetingToken(testMeetingId))
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(get("/videocalls/{meetingId}/token", testMeetingId))
                .andExpect(status().isInternalServerError());
    }
}