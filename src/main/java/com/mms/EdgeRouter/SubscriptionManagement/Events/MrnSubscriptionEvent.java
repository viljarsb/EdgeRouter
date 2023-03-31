package com.mms.EdgeRouter.SubscriptionManagement.Events;

import lombok.NonNull;

public class MrnSubscriptionEvent
{
    private final String mrn;
    private final SubscriptionEventType eventType;

    public MrnSubscriptionEvent(@NonNull String mrn, @NonNull SubscriptionEventType eventType)
    {
        this.mrn = mrn;
        this.eventType = eventType;
    }

    public String getMrn()
    {
        return mrn;
    }

    public SubscriptionEventType getEventType()
    {
        return eventType;
    }
}
