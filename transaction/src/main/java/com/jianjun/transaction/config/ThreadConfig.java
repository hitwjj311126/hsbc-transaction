package com.jianjun.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration class for thread management.
 * Configures the thread pool executor for async operations.
 */
@Configuration
@EnableAsync
public class ThreadConfig {

    /**
     * Creates a thread pool task executor for async operations.
     * Uses virtual threads for better scalability and performance.
     * @return Configured Executor instance
     */
    @Bean
    public Executor taskExecutor() {
        // Adapter needed because Spring expects a TaskExecutor
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    // Adapter needed because Spring expects a TaskExecutor
} 