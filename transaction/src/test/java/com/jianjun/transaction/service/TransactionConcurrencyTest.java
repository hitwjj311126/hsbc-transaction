package com.jianjun.transaction.service;

import com.jianjun.transaction.model.Transaction;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionConcurrencyTest {

    private TransactionService transactionService;
    private ExecutorService executorService;
    
    @Mock
    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionServiceImpl(validator);
        
        // Configure validator to pass all validations
        when(validator.validate(any(Transaction.class))).thenReturn(new HashSet<>());
        
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (executorService != null) {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    @Test
    void testConcurrentTransactions() throws Exception {
        int numThreads = 10;
        int numTransactionsPerThread = 100;
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < numTransactionsPerThread; j++) {
                    try {
                        Transaction transaction = new Transaction(
                            "ACC" + UUID.randomUUID().toString().substring(0, 8),
                            new BigDecimal(Math.random() * 1000),
                            Math.random() > 0.5 ? "CREDIT" : "DEBIT",
                            "Concurrent test transaction"
                        );
                        
                        transactionService.createTransaction(transaction).join();
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    }
                }
            }, executorService);
            
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        int totalAttempts = numThreads * numTransactionsPerThread;
        assertEquals(totalAttempts, successCount.get() + failureCount.get());
        assertTrue(successCount.get() > 0, "At least some transactions should succeed");
    }

    @Test
    void concurrentUpdateTransactions_ShouldMaintainConsistency() throws Exception {
        // Arrange
        Transaction transaction = new Transaction(
            "ACCT123456",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        Transaction created = transactionService.createTransaction(transaction).join();
        int numThreads = 10;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10; j++) {
                    Transaction update = new Transaction(
                        "ACCT123456",
                        new BigDecimal("200.00"),
                        "DEBIT",
                        "Updated transaction"
                    );
                    transactionService.updateTransaction(created.getId(), update).join();
                }
            }, executorService);
            futures.add(future);
        }

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Assert
        Transaction finalTransaction = transactionService.getTransaction(created.getId()).join();
        assertEquals("DEBIT", finalTransaction.getType());
        assertEquals(new BigDecimal("200.00"), finalTransaction.getAmount());
    }

    @Test
    void concurrentDeleteAndGet_ShouldHandleRaceConditions() throws Exception {
        // Arrange
        Transaction transaction = new Transaction(
            "ACCT123456",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );
        Transaction created = transactionService.createTransaction(transaction).join();
        int numThreads = 10;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    transactionService.getTransaction(created.getId()).join();
                    transactionService.deleteTransaction(created.getId()).join();
                } catch (Exception e) {
                    // Expected exceptions are okay
                }
            }, executorService);
            futures.add(future);
        }

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Assert
        assertThrows(Exception.class,
            () -> transactionService.getTransaction(created.getId()).join()
        );
    }
} 