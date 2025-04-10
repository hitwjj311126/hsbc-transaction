package com.jianjun.transaction.exception;

/**
 * Exception thrown when a requested transaction cannot be found in the system.
 * This typically occurs when attempting to retrieve, update, or delete
 * a transaction with an ID that doesn't exist.
 */
public class TransactionNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new TransactionNotFoundException with the specified detail message.
     * @param message The detail message explaining the reason for the exception
     */
    public TransactionNotFoundException(String message) {
        super(message);
    }
} 