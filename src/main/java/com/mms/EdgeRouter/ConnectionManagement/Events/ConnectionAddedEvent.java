package com.mms.EdgeRouter.ConnectionManagement.Events;

import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.context.ApplicationEvent;

import java.util.Optional;

public class ConnectionAddedEvent extends ApplicationEvent
{
    private final String agentID;
    private final Optional<PKIIdentity> identity;

    public ConnectionAddedEvent(Object source, String agentID, PKIIdentity identity)
    {
        super(source);
        this.agentID = agentID;
        this.identity = Optional.ofNullable(identity);
    }

    public String getAgentID()
    {
        return agentID;
    }

    public Optional<PKIIdentity> getIdentity()
    {
        return identity;
    }
}
