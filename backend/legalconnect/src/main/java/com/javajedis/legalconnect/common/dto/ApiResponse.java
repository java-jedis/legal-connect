package com.javajedis.legalconnect.common.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @JsonProperty("version")
    private String version = "v1";
    
    @JsonProperty("data")
    private T data;

    @JsonProperty("error")
    private ErrorResponse error;

    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;

    @JsonProperty("path")
    private String path;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public ApiResponse(T data, ErrorResponse error, int status, String message) {
        this.data = data;
        this.error = error;
        this.status = status;
        this.message = message;
        this.timestamp = OffsetDateTime.now();
        this.path = getCurrentRequestPath();
    }

    /**
     * Use this for simple success responses with just data and status.
     * Example: GET requests returning a single resource or list.
     * 
     * @param data The response data
     * @param status HTTP status code (e.g., HttpStatus.OK, HttpStatus.CREATED)
     * @param message Success message for the client
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(data, null, status.value(), message));
    }

    /**
     * Use this for success responses that need additional metadata.
     * Example: Paginated responses, responses with extra context, or when you need to include
     * additional information like pagination details, filtering info, or operation metadata.
     * 
     * @param data The response data
     * @param status HTTP status code
     * @param message Success message for the client
     * @param metadata Additional information (pagination, filters, context, etc.)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, HttpStatus status, String message, Map<String, Object> metadata) {
        ApiResponse<T> response = new ApiResponse<>(data, null, status.value(), message);
        response.metadata = metadata;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Use this for simple error responses with just an error message.
     * Example: Basic validation errors, simple business rule violations.
     * 
     * @param errorMessage User-friendly error message
     * @param status HTTP status code (e.g., HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND)
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String errorMessage, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), errorMessage, null);
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(null, errorResponse, status.value(), null));
    }

    /**
     * Use this for error responses that need additional technical details for debugging.
     * Example: Server errors, complex business logic errors, or when you want to provide
     * more context to developers while keeping the main message user-friendly.
     * 
     * @param errorMessage User-friendly error message
     * @param details Technical details for debugging (can be null)
     * @param status HTTP status code
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String errorMessage, String details, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), errorMessage, details);
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(null, errorResponse, status.value(), null));
    }

    private String getCurrentRequestPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes.getRequest() != null) {
                return attributes.getRequest().getRequestURI();
            }
        } catch (Exception e) {
            // Fallback if request context is not available
        }
        return null;
    }

    // Getters
    public String getVersion() { return version; }
    public T getData() { return data; }
    public ErrorResponse getError() { return error; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public Map<String, Object> getMetadata() { return metadata; }
}

