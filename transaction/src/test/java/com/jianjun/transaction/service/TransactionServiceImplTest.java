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

class TransactionServiceImplTest {

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
    void createTransaction_ShouldSucceed() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Act
        Transaction created = transactionService.createTransaction(transaction).join();

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(transaction.getAccountNumber(), created.getAccountNumber());
        assertEquals(transaction.getAmount(), created.getAmount());
        assertEquals(transaction.getType(), created.getType());
        assertEquals(transaction.getDescription(), created.getDescription());
    }

    @Test
    void createTransaction_WithDuplicate_ShouldThrowException() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Act
        transactionService.createTransaction(transaction).join();

        // Assert
        assertThrows(DuplicateTransactionException.class,
            () -> transactionService.createTransaction(transaction).join()
        );
    }

    @Test
    void getTransaction_ShouldSucceed() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        Transaction created = transactionService.createTransaction(transaction).join();

        // Act
        Transaction retrieved = transactionService.getTransaction(created.getId()).join();

        // Assert
        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(transaction.getAccountNumber(), retrieved.getAccountNumber());
        assertEquals(transaction.getAmount(), retrieved.getAmount());
        assertEquals(transaction.getType(), retrieved.getType());
        assertEquals(transaction.getDescription(), retrieved.getDescription());
    }

    @Test
    void getTransaction_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        assertThrows(TransactionNotFoundException.class,
            () -> transactionService.getTransaction(nonExistentId).join()
        );
    }

    @Test
    void updateTransaction_ShouldSucceed() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        Transaction created = transactionService.createTransaction(transaction).join();

        // Act
        created.setAmount(new BigDecimal("200.00"));
        Transaction updated = transactionService.updateTransaction(created.getId(), created).join();

        // Assert
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(new BigDecimal("200.00"), updated.getAmount());
    }

    @Test
    void updateTransaction_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Act & Assert
        assertThrows(TransactionNotFoundException.class,
            () -> transactionService.updateTransaction(nonExistentId, transaction).join()
        );
    }

    @Test
    void deleteTransaction_ShouldSucceed() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        Transaction created = transactionService.createTransaction(transaction).join();

        // Act
        transactionService.deleteTransaction(created.getId()).join();

        // Assert
        assertThrows(TransactionNotFoundException.class,
            () -> transactionService.getTransaction(created.getId()).join()
        );
    }

    @Test
    void deleteTransaction_WithNonExistentId_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        assertThrows(TransactionNotFoundException.class,
            () -> transactionService.deleteTransaction(nonExistentId).join()
        );
    }
} 