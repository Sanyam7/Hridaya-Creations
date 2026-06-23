package com.hridayacreations.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Core application wiring: enables typed configuration properties and provides the async executor
 * used for non-blocking work (audit logging, email dispatch).
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class ApplicationConfig {

    /**
     * Bounded thread pool backing {@code @Async} methods so background work cannot exhaust threads.
     */
    @Bean(name = "applicationTaskExecutor")
    public TaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("hridaya-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
