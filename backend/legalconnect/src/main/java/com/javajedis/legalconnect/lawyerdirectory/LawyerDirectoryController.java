package com.javajedis.legalconnect.lawyerdirectory;


import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.javajedis.legalconnect.common.dto.ApiResponse;
import com.javajedis.legalconnect.common.security.RequireUserOrVerifiedLawyer;
import com.javajedis.legalconnect.lawyerdirectory.dto.CreateLawyerReviewDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.FindLawyersDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerReviewListResponseDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerReviewResponseDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.LawyerSearchResultDTO;
import com.javajedis.legalconnect.lawyerdirectory.dto.UpdateLawyerReviewDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "6. Lawyer Directory", description = "Lawyer directory and review management endpoints")
@RestController
@RequestMapping("/lawyer-directory")
public class LawyerDirectoryController {
    private final LawyerDirectoryService lawyerDirectoryService;

    public LawyerDirectoryController(LawyerDirectoryService lawyerDirectoryService) {
        this.lawyerDirectoryService = lawyerDirectoryService;
    }

    @Operation(summary = "Find lawyers", description = "Finds lawyers based on the provided criteria.")
    @PostMapping("/find-lawyers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<LawyerSearchResultDTO>>> findLawyers(
            @Valid @RequestBody FindLawyersDTO findLawyersDTO,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("POST /lawyer-directory/find-lawyers called with filters: {}, page: {}, size: {}, sort: {}", findLawyersDTO, page, size, sortDirection);
        return lawyerDirectoryService.findLawyers(findLawyersDTO, page, size, sortDirection);
    }

    @Operation(summary = "Add a review for a lawyer", description = "Adds a review for a lawyer.")
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> addReview(
            @Valid @RequestBody CreateLawyerReviewDTO createLawyerReviewDTO) {
        log.info("POST /lawyer-directory/reviews called for case: {}", createLawyerReviewDTO.getCaseId());
        return lawyerDirectoryService.addReview(createLawyerReviewDTO);
    }

    @Operation(summary = "Update a review", description = "Updates a review.")
    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateLawyerReviewDTO updateLawyerReviewDTO) {
        log.info("PUT /lawyer-directory/reviews/{} called", reviewId);
        return lawyerDirectoryService.updateReview(reviewId, updateLawyerReviewDTO);
    }

    @Operation(summary = "Delete a review", description = "Deletes a review.")
    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteReview(@PathVariable UUID reviewId) {
        log.info("DELETE /lawyer-directory/reviews/{} called", reviewId);
        return lawyerDirectoryService.deleteReview(reviewId);
    }

    @Operation(summary = "Get a review by case ID", description = "Gets a review by caseID.")
    @GetMapping("/reviews/{caseId}")
    @RequireUserOrVerifiedLawyer
    public ResponseEntity<ApiResponse<LawyerReviewResponseDTO>> getReview(@PathVariable UUID caseId) {
        log.info("GET /lawyer-directory/reviews/{} called", caseId);
        return lawyerDirectoryService.getReview(caseId);
    }

    @Operation(summary = "Get all reviews for a lawyer", description = "Gets all reviews for a lawyer.")
    @GetMapping("/lawyers/{lawyerId}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<LawyerReviewListResponseDTO>> getReviews(
            @PathVariable UUID lawyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("GET /lawyer-directory/lawyers/{}/reviews called with page={}, size={}, sortDirection={}", lawyerId, page, size, sortDirection);
        return lawyerDirectoryService.getReviews(lawyerId, page, size, sortDirection);
    }
}
