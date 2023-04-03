package com.mms.EdgeRouter.SubscriptionManagement.Events;

import lombok.NonNull;
import org.springframework.context.ApplicationEvent;

/**
 * This class represents an MRN subscription event.
 */
public class MrnSubscriptionEvent extends ApplicationEvent
{

    private final String mrn;
    private final SubscriptionEventType eventType;


    /**
     * Constructs a new instance of MrnSubscriptionEvent.
     *
     * @param mrn       The MRN associated with the event. Must not be null.
     * @param eventType The SubscriptionEventType associated with the event. Must not be null.
     */
    public MrnSubscriptionEvent(@NonNull Object source, @NonNull String mrn, @NonNull SubscriptionEventType eventType)
    {
        super(source);
        this.mrn = mrn;
        this.eventType = eventType;
    }


    /**
     * Returns the MRN associated with the event.
     *
     * @return The MRN associated with the event.
     */
    public String getMrn()
    {
        return mrn;
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





