package com.jianjun.transaction.controller;

import com.jianjun.transaction.model.Transaction;
import com.jianjun.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for managing transactions.
 * Provides endpoints for CRUD operations on transactions.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Constructor for TransactionController.
     * @param transactionService The transaction service to use
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new transaction.
     * @param transaction The transaction to create
     * @return ResponseEntity containing the created transaction
     */
    @PostMapping
    public CompletableFuture<ResponseEntity<Transaction>> createTransaction(@Valid @RequestBody Transaction transaction) {
        return transactionService.createTransaction(transaction)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Retrieves a transaction by ID.
     * @param id The ID of the transaction to retrieve
     * @return ResponseEntity containing the transaction
     */
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Transaction>> getTransaction(@PathVariable UUID id) {
        return transactionService.getTransaction(id)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Updates an existing transaction.
     * @param id The ID of the transaction to update
     * @param transaction The updated transaction data
     * @return ResponseEntity containing the updated transaction
     */
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<Transaction>> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody Transaction transaction) {
        return transactionService.updateTransaction(id, transaction)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Deletes a transaction by ID.
     * @param id The ID of the transaction to delete
     * @return ResponseEntity with no content
     */
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteTransaction(@PathVariable UUID id) {
        return transactionService.deleteTransaction(id)
                .thenApply(v -> ResponseEntity.noContent().build());
    }

    /**
     * Retrieves all transactions with pagination.
     * @param pageable Pagination information
     * @return ResponseEntity containing a page of transactions
     */
    @GetMapping
    public CompletableFuture<ResponseEntity<Page<Transaction>>> getAllTransactions(Pageable pageable) {
        return transactionService.getAllTransactions(pageable)
                .thenApply(ResponseEntity::ok);
    }
} 