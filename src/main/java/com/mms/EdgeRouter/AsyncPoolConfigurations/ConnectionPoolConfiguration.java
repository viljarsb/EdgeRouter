package com.mms.EdgeRouter.AsyncPoolConfigurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The ConnectionPoolConfiguration class configures the thread pool used for handling connection related async tasks.
 * It creates a TaskExecutor bean named "ConnectionPool" with the specified core pool size, max pool size, keep-alive
 * time, and queue capacity, as specified by the config, if no values are given, some defaults are used.
 */
@Slf4j
@EnableAsync(proxyTargetClass = true)
@Configuration
public class ConnectionPoolConfiguration
{
    @Value("${connection-pool.core-pool-size:10}")
    private int corePoolSize;

    @Value("${connection-pool.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${connection-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${connection-pool.queue-capacity:1000}")
    private int queueCapacity;

    @Bean(name = "ConnectionPool") // Give the bean a name so that Spring knows which TaskExecutor to use
    public TaskExecutor connectionTaskExecutor()
    {
        log.info("Initializing ConnectionPool with corePoolSize={}, maxPoolSize={}, keepAliveSeconds={}, queueCapacity={}", corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity);
        ExecutorService service = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
        return new ConcurrentTaskExecutor(service);
    }
}
