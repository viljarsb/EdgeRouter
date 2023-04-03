package com.mms.EdgeRouter.AsyncTaskManagement;

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

@Slf4j
@EnableAsync(proxyTargetClass = true)
@Configuration
public class WorkerPoolConfiguration
{
    @Value("${worker-pool.core-pool-size:10}")
    private int corePoolSize;

    @Value("${worker-pool.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${worker-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${worker-pool.queue-capacity:1000}")
    private int queueCapacity;

    @Bean(name = "WorkerPool") // Give the bean a name so that Spring knows which TaskExecutor to use
    public TaskExecutor workerPoolExecutor()
    {
        log.info("Initializing WorkerPool with corePoolSize={}, maxPoolSize={}, keepAliveSeconds={}, queueCapacity={}", corePoolSize, maxPoolSize, keepAliveSeconds, queueCapacity);
        ExecutorService service = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
        return new ConcurrentTaskExecutor(service);
    }
}
