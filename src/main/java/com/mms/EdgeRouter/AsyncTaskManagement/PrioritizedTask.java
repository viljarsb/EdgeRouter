package com.mms.EdgeRouter.AsyncTaskManagement;

import java.util.concurrent.PriorityBlockingQueue;

public interface PrioritizedTask extends Runnable, Comparable<PrioritizedTask> {

    TaskPriority getPriority();

    @Override
    default int compareTo(PrioritizedTask other) {
        return this.getPriority().compareTo(other.getPriority());
    }

    static PriorityBlockingQueue<PrioritizedTask> createPriorityBlockingQueue() {
        return new PriorityBlockingQueue<>();
    }

}


