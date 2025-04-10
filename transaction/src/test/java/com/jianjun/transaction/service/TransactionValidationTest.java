package com.jianjun.transaction.service;

import com.jianjun.transaction.model.Transaction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionValidationTest {

    private TransactionService transactionService;
    
    @Mock
    private Validator validator;

    @Mock
    private ConstraintViolation<Transaction> violation;

    @Mock
    private Path path;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionServiceImpl(validator);
    }

    @Test
    void createTransaction_WithInvalidAccountNumber_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "abc123", // Invalid: too short and contains letters
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        Set<ConstraintViolation<Transaction>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(Transaction.class))).thenReturn(violations);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("accountNumber");

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }

    @Test
    void createTransaction_WithTooLongAccountNumber_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "1234567890123", // Invalid: 13 digits (too long)
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        Set<ConstraintViolation<Transaction>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(Transaction.class))).thenReturn(violations);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("accountNumber");

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }

    @Test
    void createTransaction_WithInvalidAmount_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("-100.00"), // Negative amount
            "CREDIT",
            "Test transaction"
        );

        Set<ConstraintViolation<Transaction>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(Transaction.class))).thenReturn(violations);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("amount");

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }

    @Test
    void createTransaction_WithInvalidType_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "INVALID", // Invalid type
            "Test transaction"
        );

        Set<ConstraintViolation<Transaction>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(Transaction.class))).thenReturn(violations);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("type");

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }

    @Test
    void createTransaction_WithEmptyDescription_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "" // Empty description
        );

        Set<ConstraintViolation<Transaction>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(Transaction.class))).thenReturn(violations);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("description");

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }

    @Test
    void createTransaction_WithTooLongDescription_ShouldThrowException() {
        // Arrange
        String longDescription = "a".repeat(256); // 256 characters
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            longDescription
        );

        Set<ConstraintViolation<Transaction>> violations = new HashSet<>();
        violations.add(violation);
        when(validator.validate(any(Transaction.class))).thenReturn(violations);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("description");

        // Act & Assert
        assertThrows(ConstraintViolationException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }
} 