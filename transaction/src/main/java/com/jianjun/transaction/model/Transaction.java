package com.jianjun.transaction.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a financial transaction in the system.
 * This class includes validation constraints and business rules
 * for transaction data.
 */
public class Transaction {
    // Unique identifier for the transaction
    private UUID id;
    
    // Account number with validation constraints
    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{8,16}$", message = "Account number must be 8-16 digits")
    private String accountNumber;
    
    // Transaction amount with validation constraints
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    // Transaction type with validation constraints
    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(DEBIT|CREDIT)$", message = "Type must be either DEBIT or CREDIT")
    private String type;
    
    // Transaction description with validation constraints
    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    // Timestamp of when the transaction was created
    private LocalDateTime timestamp;
    
    // Current status of the transaction
    @Pattern(regexp = "^(PENDING|COMPLETED|FAILED)$", message = "Status must be PENDING, COMPLETED, or FAILED")
    private String status;
    
    /**
     * Default constructor.
     * Initializes the transaction with a new UUID and current timestamp.
     */
    public Transaction() {
        this.id = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Constructor with required fields.
     * @param accountNumber The account number for the transaction
     * @param amount The transaction amount
     * @param type The transaction type (DEBIT/CREDIT)
     * @param description A description of the transaction
     */
    public Transaction(String accountNumber, BigDecimal amount, String type, String description) {
        this();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.status = "PENDING";
    }
    
    // Getters and Setters with documentation
    /**
     * @return The unique identifier of the transaction
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * @param id The unique identifier to set
     */
    public void setId(UUID id) {
        this.id = id;
    }
    
    /**
     * @return The account number associated with the transaction
     */
    public String getAccountNumber() {
        return accountNumber;
    }
    
    /**
     * @param accountNumber The account number to set
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    /**
     * @return The amount of the transaction
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * @param amount The amount to set
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    /**
     * @return The type of transaction (DEBIT/CREDIT)
     */
    public String getType() {
        return type;
    }
    
    /**
     * @param type The transaction type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * @return The description of the transaction
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return The timestamp when the transaction was created
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * @param timestamp The timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * @return The current status of the transaction
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * @param status The status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
} 