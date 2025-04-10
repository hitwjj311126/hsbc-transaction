package com.jianjun.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for the Transaction Service.
 * This is a Spring Boot application that provides a high-performance,
 * concurrent transaction management system with in-memory storage.
 * 
 * Features enabled:
 * - Caching for improved performance
 * - Async processing for better concurrency
 * - Spring Boot auto-configuration
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class TransactionApplication {

	/**
	 * Main method to start the Spring Boot application.
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(TransactionApplication.class, args);
	}

}
