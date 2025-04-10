package com.jianjun.transaction.controller;

import com.jianjun.transaction.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionErrorHandlingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createTransaction_WithInvalidAccountNumber_ShouldReturnValidationError() {
        // Arrange
        Transaction transaction = new Transaction(
            "123", // Too short
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/transactions",
            transaction,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Error", response.getBody().get("error"));
        assertTrue(response.getBody().containsKey("details"));
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertTrue(details.containsKey("accountNumber"));
    }

    @Test
    void createTransaction_WithInvalidAmount_ShouldReturnValidationError() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("-100.00"), // Negative amount
            "CREDIT",
            "Test transaction"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/transactions",
            transaction,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Error", response.getBody().get("error"));
        assertTrue(response.getBody().containsKey("details"));
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertTrue(details.containsKey("amount"));
    }

    @Test
    void createTransaction_WithInvalidType_ShouldReturnValidationError() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "INVALID", // Invalid type
            "Test transaction"
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/transactions",
            transaction,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Error", response.getBody().get("error"));
        assertTrue(response.getBody().containsKey("details"));
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertTrue(details.containsKey("type"));
    }

    @Test
    void createTransaction_WithEmptyDescription_ShouldReturnValidationError() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "" // Empty description
        );

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/transactions",
            transaction,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Error", response.getBody().get("error"));
        assertTrue(response.getBody().containsKey("details"));
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertTrue(details.containsKey("description"));
    }

    @Test
    void getTransaction_WithNonExistentId_ShouldReturnNotFoundError() {
        // Arrange
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // Act
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/api/transactions/" + nonExistentId,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Transaction Not Found", response.getBody().get("error"));
    }

    @Test
    void createTransaction_WithDuplicateData_ShouldReturnConflictError() {
        // Arrange
        Transaction transaction = new Transaction(
            "12345678",
            new BigDecimal("100.00"),
            "CREDIT",
            "Test transaction"
        );

        // Create first transaction
        restTemplate.postForEntity("/api/transactions", transaction, Transaction.class);

        // Act - Try to create duplicate
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/transactions",
            transaction,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Duplicate Transaction", response.getBody().get("error"));
    }

    @Test
    void createTransaction_WithNullFields_ShouldReturnValidationError() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(null);
        transaction.setAmount(null);
        transaction.setType(null);
        transaction.setDescription(null);

        // Act
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/transactions",
            transaction,
            Map.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Error", response.getBody().get("error"));
        assertTrue(response.getBody().containsKey("details"));
        Map<String, String> details = (Map<String, String>) response.getBody().get("details");
        assertTrue(details.containsKey("accountNumber"));
        assertTrue(details.containsKey("amount"));
        assertTrue(details.containsKey("type"));
        assertTrue(details.containsKey("description"));
    }
} 