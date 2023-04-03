package com.mms.EdgeRouter.MessageHandler.Events;

import com.mms.EdgeRouter.SubscriptionManagement.Events.SubscriptionEventType;
import org.springframework.context.ApplicationEvent;

/**
 * An event used to request a subscription to MRN directed messages.
 */
public class SubscriptionMrnRequest extends ApplicationEvent
{
    private final String agentID;
    private final SubscriptionEventType type;

    /**
     * Constructs a new SubscriptionMrnRequest with the given dependencies.
     *
     * @param source  The source of the event.
     * @param agentID The ID of the agent requesting the subscription.
     * @param type    The type of subscription event to request.
     */
    public SubscriptionMrnRequest(Object source, String agentID, SubscriptionEventType type)
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
