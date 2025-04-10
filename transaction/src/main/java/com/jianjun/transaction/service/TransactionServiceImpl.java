package com.jianjun.transaction.service;

import com.jianjun.transaction.exception.TransactionNotFoundException;
import com.jianjun.transaction.exception.DuplicateTransactionException;
import com.jianjun.transaction.model.Transaction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Caching;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Implementation of the TransactionService interface.
 * Provides thread-safe transaction management with in-memory storage and caching.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    // Primary storage for transactions using UUID as key
    private final Map<UUID, Transaction> transactionStore = new ConcurrentHashMap<>();
    
    // Secondary storage for duplicate detection using composite key
    private final Map<String, Transaction> accountTransactionStore = new ConcurrentHashMap<>();
    
    // Validator for transaction data
    private final Validator validator;

    /**
     * Constructor for TransactionServiceImpl.
     * @param validator Jakarta Validation validator instance
     */
    public TransactionServiceImpl(Validator validator) {
        this.validator = validator;
    }

    /**
     * Creates a new transaction with validation and duplicate checking.
     * @param transaction The transaction to create
     * @return CompletableFuture containing the created transaction
     * @throws IllegalArgumentException if transaction is null
     * @throws ConstraintViolationException if validation fails
     * @throws DuplicateTransactionException if a similar transaction exists
     */
    @Override
    @Async
    @CachePut(value = "transactions", key = "#result.id")
    // @CacheEvict(value = "transactions", allEntries = true)
    public CompletableFuture<Transaction> createTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        // Validate transaction data
        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        logger.debug("Attempting to create transaction for account: {}", transaction.getAccountNumber());
        
        // Check for duplicate transactions
        String key = generateTransactionKey(transaction);
        if (accountTransactionStore.containsKey(key)) {
            logger.warn("Duplicate transaction attempt detected for account: {}, amount: {}, type: {}", 
                transaction.getAccountNumber(), transaction.getAmount(), transaction.getType());
            throw new DuplicateTransactionException("A similar transaction already exists for this account");
        }

        // Generate UUID if not provided
        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID());
        }
        
        // Store transaction in both maps
        transactionStore.put(transaction.getId(), transaction);
        accountTransactionStore.put(key, transaction);
        
        logger.info("Successfully created transaction with ID: {} for account: {}", 
            transaction.getId(), transaction.getAccountNumber());
        return CompletableFuture.completedFuture(transaction);
    }

    /**
     * Updates an existing transaction with validation and duplicate checking.
     * @param id The ID of the transaction to update
     * @param transaction The updated transaction data
     * @return CompletableFuture containing the updated transaction
     * @throws IllegalArgumentException if id or transaction is null
     * @throws ConstraintViolationException if validation fails
     * @throws TransactionNotFoundException if transaction not found
     */
    @Override
    @Async
    
    @CachePut(value = "transactions", key = "#id")
    public CompletableFuture<Transaction> updateTransaction(UUID id, Transaction transaction) {
        if (id == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }

        // Validate transaction data
        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        // Check if transaction exists
        if (!transactionStore.containsKey(id)) {
            logger.warn("Transaction not found for update with ID: {}", id);
            throw new TransactionNotFoundException("Transaction not found with ID: " + id);
        }

        // Update transaction
        transaction.setId(id);
        String key = generateTransactionKey(transaction);
        transactionStore.put(id, transaction);
        accountTransactionStore.put(key, transaction);

        logger.info("Successfully updated transaction with ID: {}", id);
        return CompletableFuture.completedFuture(transaction);
    }

    /**
     * Deletes a transaction by ID.
     * @param id The ID of the transaction to delete
     * @return CompletableFuture indicating completion
     * @throws TransactionNotFoundException if transaction not found
     */
    @Override
    @Async
    @Caching(
        evict = {
            @CacheEvict(value = "transactions", key = "#id")
        }
    )
    public CompletableFuture<Void> deleteTransaction(UUID id) {
        logger.debug("Attempting to delete transaction with ID: {}", id);
        
        // Remove from primary store
        Transaction transaction = transactionStore.remove(id);
        if (transaction == null) {
            logger.warn("Transaction not found for deletion with ID: {}", id);
            throw new TransactionNotFoundException("Transaction not found with id: " + id);
        }

        // Remove from account store
        String key = generateTransactionKey(transaction);
        accountTransactionStore.remove(key);
        
        logger.info("Successfully deleted transaction with ID: {}", id);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Retrieves a transaction by its ID.
     * @param id The ID of the transaction to retrieve
     * @return CompletableFuture containing the transaction
     * @throws IllegalArgumentException if id is null
     * @throws TransactionNotFoundException if transaction not found
     */
    @Override
    @Async
    @Cacheable(value = "transactions", key = "#id")
    public CompletableFuture<Transaction> getTransaction(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }

        Transaction transaction = transactionStore.get(id);
        if (transaction == null) {
            logger.warn("Transaction not found with ID: {}", id);
            throw new TransactionNotFoundException("Transaction not found with ID: " + id);
        }

        return CompletableFuture.completedFuture(transaction);
    }

    /**
     * Retrieves all transactions with pagination.
     * @param pageable Pagination information
     * @return CompletableFuture containing a page of transactions
     */
    @Override
    @Async
    public CompletableFuture<Page<Transaction>> getAllTransactions(Pageable pageable) {
        logger.debug("Retrieving all transactions with page: {}, size: {}", 
            pageable.getPageNumber(), pageable.getPageSize());
        
        // Get all transactions and apply pagination
        List<Transaction> transactions = new ArrayList<>(transactionStore.values());
        
        // Sort transactions by timestamp (most recent first)
        transactions.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), transactions.size());
        
        List<Transaction> pageContent = transactions.subList(start, end);
        Page<Transaction> page = new PageImpl<>(pageContent, pageable, transactions.size());
        
        logger.debug("Retrieved {} transactions out of total {}", pageContent.size(), transactions.size());
        return CompletableFuture.completedFuture(page);
    }

    /**
     * Generates a unique key for a transaction based on account number, amount, and type.
     * Used for duplicate detection.
     * @param transaction The transaction to generate key for
     * @return A string key combining account number, amount, and type
     */
    private String generateTransactionKey(Transaction transaction) {
        return String.format("%s-%s-%s", 
            transaction.getAccountNumber(), 
            transaction.getAmount(), 
            transaction.getType());
    }
} 