package com.mms.EdgeRouter.MessageHandler.Events;

import com.mms.EdgeRouter.SubscriptionManagement.Events.SubscriptionEventType;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * An event used to request a subscription to subjects.
 */
public class SubscriptionSubjectRequest extends ApplicationEvent
{
    private final String agentID;
    private final List<String> subjects;
    private final SubscriptionEventType type;


    /**
     * Constructs a new SubscriptionSubjectRequest with the given dependencies.
     *
     * @param source   The source of the event.
     * @param agentID  The ID of the agent requesting the subscription.
     * @param subjects The list of subjects to subscribe to.
     * @param type     The type of subscription event to request.
     */
    public SubscriptionSubjectRequest(Object source, String agentID, List<String> subjects, SubscriptionEventType type)
    {
        super(source);
        this.agentID = agentID;
        this.subjects = subjects;
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
     * Returns the list of subjects to subscribe to.
     *
     * @return The list of subjects to subscribe to.
     */
    public List<String> getSubjects()
    {
        return subjects;
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
