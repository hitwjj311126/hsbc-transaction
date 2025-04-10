package com.jianjun.transaction;

import com.jianjun.transaction.model.Transaction;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TransactionValidationTest {

    @Autowired
    private LocalValidatorFactoryBean validator;

    @Test
    public void testAccountNumberTooShort() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber("1234567"); // 7 digits
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType("DEBIT");
        transaction.setDescription("Test transaction");
        transaction.setStatus("PENDING");

        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Account number must be 8-16 digits")));
    }

    @Test
    public void testAccountNumberTooLong() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber("12345678901234567"); // 17 digits
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType("DEBIT");
        transaction.setDescription("Test transaction");
        transaction.setStatus("PENDING");

        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Account number must be 8-16 digits")));
    }

    @Test
    public void testValidAccountNumber() {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber("123456789012"); // 12 digits
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType("DEBIT");
        transaction.setDescription("Test transaction");
        transaction.setStatus("PENDING");

        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);
        assertTrue(violations.isEmpty());
    }
} 