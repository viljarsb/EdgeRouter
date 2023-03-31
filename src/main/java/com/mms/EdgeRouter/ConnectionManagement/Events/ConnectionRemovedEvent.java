package com.mms.EdgeRouter.ConnectionManagement.Events;


import org.springframework.context.ApplicationEvent;

public class ConnectionRemovedEvent extends ApplicationEvent
{
    private String AgentID;

    public ConnectionRemovedEvent(Object source, String AgentID)
    {
        super(source);
        this.AgentID = AgentID;
    }

    public String getAgentID()
    {
        return AgentID;
    }

}