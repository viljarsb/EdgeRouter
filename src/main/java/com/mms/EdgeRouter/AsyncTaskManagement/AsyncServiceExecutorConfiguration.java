package com.mms.EdgeRouter.AsyncTaskManagement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncServiceExecutorConfiguration
{
    @Value("${thread-pool.core-pool-size:10}")
    private int corePoolSize;

    @Value("${thread-pool.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${thread-pool.queue-capacity:1000}")
    private int queueCapacity;

    @Bean
    public AsyncServiceExecutor asyncServiceExecutor()
    {
        return new AsyncServiceExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity);
    }
}
