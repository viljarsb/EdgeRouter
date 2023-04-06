package com.mms.EdgeRouter.MessageHandlers.Events;

import com.mms.EdgeRouter.SubscriptionManagement.Events.SubscriptionEventType;
import org.springframework.context.ApplicationEvent;

/**
 * An event used to request a subscription to MRN directed messages.
 */
public class MrnSubscriptionRequestEvent extends ApplicationEvent
{
    private final String agentID;
    private final SubscriptionEventType type;

    /**
     * Constructs a new MrnSubscriptionRequestEvent with the given dependencies.
     *
     * @param source  The source of the event.
     * @param agentID The ID of the agent requesting the subscription.
     * @param type    The type of subscription event to request.
     */
    public MrnSubscriptionRequestEvent(Object source, String agentID, SubscriptionEventType type)
    {
        super(source);
        this.agentID = agentID;
        this.type = type;
    }

    /**
     * Returns the ID of the agent requesting the subscription.
     *
     * @return The ID of the agent requesting the subscription.
     */
    public String getAgentID()
    {
        return agentID;
    }

    /**
     * Returns the type of subscription event to request.
     *
     * @return The type of subscription event to request.
     */
    public SubscriptionEventType getType()
    {
        return type;
    }
}
