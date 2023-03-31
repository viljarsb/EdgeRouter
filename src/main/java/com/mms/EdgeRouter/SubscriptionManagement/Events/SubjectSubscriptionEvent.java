package com.mms.EdgeRouter.SubscriptionManagement.Events;

import lombok.NonNull;

import java.util.List;


public class SubjectSubscriptionEvent
{
    private final String subject;
    private final SubscriptionEventType eventType;


    public SubjectSubscriptionEvent(@NonNull String subject, @NonNull SubscriptionEventType eventType)
    {
        this.subject = subject;
        this.eventType = eventType;
    }


    public String getSubjects()
    {
        return subject;
    }


    public SubscriptionEventType getEventType()
    {
        return eventType;
    }
}
