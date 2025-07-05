package com.javajedis.legalconnect.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    @JsonProperty("code")
    private int code; // HTTP status code

    @JsonProperty("message")
    private String message;

    @JsonProperty("details")
    private String details;
}
