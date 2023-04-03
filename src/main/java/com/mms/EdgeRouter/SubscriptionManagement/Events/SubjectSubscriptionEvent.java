package com.mms.EdgeRouter.SubscriptionManagement.Events;

import lombok.NonNull;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * This class represents a subject subscription event.
 */
public class SubjectSubscriptionEvent extends ApplicationEvent
{
    private final String subject;
    private final SubscriptionEventType eventType;


    /**
     * Constructs a new instance of SubjectSubscriptionEvent.
     *
     * @param source    The object that fired the event.
     * @param subject   The subject associated with the event. Must not be null.
     * @param eventType The SubscriptionEventType associated with the event. Must not be null.
     */
    public SubjectSubscriptionEvent(@NonNull Object source, @NonNull String subject, @NonNull SubscriptionEventType eventType)
    {
        super(source);
        this.subject = subject;
        this.eventType = eventType;
    }


    /**
     * Returns the subject associated with the event.
     *
     * @return The subject associated with the event.
     */
    public String getSubjects()
    {
        return subject;
    }


    /**
     * Returns the SubscriptionEventType associated with the event.
     *
     * @return The SubscriptionEventType associated with the event.
     */
    public SubscriptionEventType getEventType()
    {
        return eventType;
    }
}
