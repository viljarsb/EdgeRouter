package com.mms.EdgeRouter.AsyncTaskManagement;



import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class AsyncServiceExecutor
{
    private final ExecutorService executorService;


    public AsyncServiceExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity)
    {
        this.executorService = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
    }


    public void runAsync(Runnable runnable, TaskPriority priority)
    {
        executorService.execute(new PrioritizedRunnable(runnable, priority));
    }


    private static class PrioritizedRunnable implements PrioritizedTask
    {
        private final Runnable runnable;
        private final TaskPriority priority;


        public PrioritizedRunnable(Runnable runnable, TaskPriority priority)
        {
            this.runnable = runnable;
            this.priority = priority;
        }


        @Override
        public void run()
        {
            runnable.run();
        }


        @Override
        public TaskPriority getPriority()
        {
            return priority;
        }
    }
}
