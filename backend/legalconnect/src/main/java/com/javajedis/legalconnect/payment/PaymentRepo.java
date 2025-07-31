package com.javajedis.legalconnect.payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.javajedis.legalconnect.user.User;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, UUID> {
    
    /**
     * Find payment by ID
     */
    Optional<Payment> findById(UUID id);
    
    /**
     * Find all payments for a specific payer
     */
    List<Payment> findByPayer(User payer);
    
    /**
     * Find all payments for a specific payee
     */
    List<Payment> findByPayee(User payee);
    
    /**
     * Find all payments for a specific payer with pagination
     */
    Page<Payment> findByPayer(User payer, Pageable pageable);
    
    /**
     * Find all payments for a specific payee with pagination
     */
    Page<Payment> findByPayee(User payee, Pageable pageable);
    
    /**
     * Find all payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);
    
    /**
     * Find all payments by status with pagination
     */
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    /**
     * Find all payments for a specific payer by status
     */
    List<Payment> findByPayerAndStatus(User payer, PaymentStatus status);
    
    /**
     * Find all payments for a specific payee by status
     */
    List<Payment> findByPayeeAndStatus(User payee, PaymentStatus status);
    
    /**
     * Find all payments for a specific payer by status with pagination
     */
    Page<Payment> findByPayerAndStatus(User payer, PaymentStatus status, Pageable pageable);
    
    /**
     * Find all payments for a specific payee by status with pagination
     */
    Page<Payment> findByPayeeAndStatus(User payee, PaymentStatus status, Pageable pageable);
    
    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Find all payments by reference ID
     */
    List<Payment> findByRefId(UUID refId);
    
    /**
     * Find all payments by reference ID and status
     */
    List<Payment> findByRefIdAndStatus(UUID refId, PaymentStatus status);
    
    /**
     * Custom query to find payments for a user (as either payer or payee)
     */
    @Query("SELECT p FROM Payment p WHERE p.payer = :user OR p.payee = :user")
    Page<Payment> findByUserAsPayerOrPayee(@Param("user") User user, Pageable pageable);
    
    /**
     * Custom query to find payments for a user (as either payer or payee) by status
     */
    @Query("SELECT p FROM Payment p WHERE (p.payer = :user OR p.payee = :user) AND p.status = :status")
    Page<Payment> findByUserAsPayerOrPayeeAndStatus(@Param("user") User user, @Param("status") PaymentStatus status, Pageable pageable);
    
    /**
     * Check if payment exists by transaction ID
     */
    boolean existsByTransactionId(String transactionId);
} 