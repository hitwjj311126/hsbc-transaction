package com.jianjun.transaction.service;

import com.jianjun.transaction.model.Transaction;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionCacheTest {

    private TransactionService transactionService;
    private CacheManager cacheManager;
    
    @Mock
    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cacheManager = new ConcurrentMapCacheManager("transactions");
        transactionService = new TransactionServiceImpl(validator);
        
        // Configure validator to pass all validations
        when(validator.validate(any(Transaction.class))).thenReturn(new HashSet<>());
    }

    @Test
    void createTransaction_ShouldCacheTransaction() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Act
        Transaction created = transactionService.createTransaction(transaction).join();
        Transaction cached = transactionService.getTransaction(created.getId()).join();

        // Assert
        assertNotNull(cached);
        assertEquals(created.getId(), cached.getId());
        assertEquals(transaction.getAmount(), cached.getAmount());
    }

    @Test
    void updateTransaction_ShouldUpdateCache() {
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
        Transaction cached = transactionService.getTransaction(created.getId()).join();

        // Assert
        assertNotNull(cached);
        assertEquals(updated.getId(), cached.getId());
        assertEquals(new BigDecimal("200.00"), cached.getAmount());
    }

    @Test
    void deleteTransaction_ShouldRemoveFromCache() {
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
        assertThrows(Exception.class,
            () -> transactionService.getTransaction(created.getId()).join()
        );
    }


}