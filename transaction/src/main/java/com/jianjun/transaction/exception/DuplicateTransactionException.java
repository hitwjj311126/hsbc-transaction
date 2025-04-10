package com.jianjun.transaction.exception;

/**
 * Exception thrown when attempting to create a transaction that would be
 * a duplicate of an existing transaction. This is determined by comparing
 * the account number, amount, and type of the transaction.
 */
public class DuplicateTransactionException extends RuntimeException {
    
    /**
     * Constructs a new DuplicateTransactionException with the specified detail message.
     * @param message The detail message explaining the reason for the exception
     */
    public DuplicateTransactionException(String message) {
        super(message);
    }
} 