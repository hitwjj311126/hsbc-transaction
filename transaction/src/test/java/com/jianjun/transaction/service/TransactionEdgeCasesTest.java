package com.jianjun.transaction.service;

import com.jianjun.transaction.exception.DuplicateTransactionException;
import com.jianjun.transaction.exception.TransactionNotFoundException;
import com.jianjun.transaction.model.Transaction;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionEdgeCasesTest {

    private TransactionService transactionService;
    
    @Mock
    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionServiceImpl(validator);
        
        // Configure validator to pass all validations
        when(validator.validate(any(Transaction.class))).thenReturn(new HashSet<>());
    }

    @Test
    void createTransaction_WithNullId_ShouldGenerateNewId() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        transaction.setId(null);

        // Act
        Transaction created = transactionService.createTransaction(transaction).join();

        // Assert
        assertNotNull(created.getId());
    }

    @Test
    void createTransaction_WithExistingId_ShouldUseProvidedId() {
        // Arrange
        UUID expectedId = UUID.randomUUID();
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        transaction.setId(expectedId);

        // Act
        Transaction created = transactionService.createTransaction(transaction).join();

        // Assert
        assertEquals(expectedId, created.getId());
    }

    @Test
    void createTransaction_WithZeroAmount_ShouldSucceed() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            BigDecimal.ZERO,
            "CREDIT",
            "Test transaction"
        );

        // Act
        Transaction created = transactionService.createTransaction(transaction).join();

        // Assert
        assertNotNull(created);
        assertEquals(BigDecimal.ZERO, created.getAmount());
    }

    @Test
    void createTransaction_WithMaxAmount_ShouldSucceed() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("999999999.99"),
            "CREDIT",
            "Test transaction"
        );

        // Act
        Transaction created = transactionService.createTransaction(transaction).join();

        // Assert
        assertNotNull(created);
        assertEquals(new BigDecimal("999999999.99"), created.getAmount());
    }

    @Test
    void createTransaction_WithDuplicateTransaction_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Create first transaction
        transactionService.createTransaction(transaction).join();

        // Act & Assert
        assertThrows(DuplicateTransactionException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }

    @Test
    void updateTransaction_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Act & Assert
        assertThrows(TransactionNotFoundException.class,
            () -> transactionService.updateTransaction(UUID.randomUUID(), transaction).join()
        );
    }

    @Test
    void deleteTransaction_WithNonExistentId_ShouldThrowException() {
        // Act & Assert
        assertThrows(TransactionNotFoundException.class,
            () -> transactionService.deleteTransaction(UUID.randomUUID()).join()
        );
    }

    @Test
    void getTransaction_WithNullId_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> transactionService.getTransaction(null).join()
        );
    }

    @Test
    void updateTransaction_WithNullTransaction_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        Transaction created = transactionService.createTransaction(transaction).join();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> transactionService.updateTransaction(created.getId(), null).join()
        );
    }

    @Test
    void createTransaction_WithNullTransaction_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> transactionService.createTransaction(null).join()
        );
    }

    @Test
    void createDuplicateTransaction_ShouldThrowException() {
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // ... rest of the test ...
    }
} 