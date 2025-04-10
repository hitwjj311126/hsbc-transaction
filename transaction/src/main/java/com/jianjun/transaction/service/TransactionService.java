package com.jianjun.transaction.service;

import com.jianjun.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TransactionService {
    CompletableFuture<Transaction> createTransaction(Transaction transaction);
    CompletableFuture<Transaction> updateTransaction(UUID id, Transaction transaction);
    CompletableFuture<Void> deleteTransaction(UUID id);
    CompletableFuture<Transaction> getTransaction(UUID id);
    CompletableFuture<Page<Transaction>> getAllTransactions(Pageable pageable);
} 