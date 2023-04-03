package com.mms.EdgeRouter.ConnectionManagement.Events;

import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.context.ApplicationEvent;

import java.util.Optional;


/**
 * An event that is published when a WebSocket connection is added to the map of active connections in the ConnectionRepository class.
 * The event contains information about the new connection, including the agentID and PKIIdentity.
 */
public class ConnectionAddedEvent extends ApplicationEvent
{
    private final String agentID;
    private final PKIIdentity identity;


    /**
     * Constructs a new ConnectionAddedEvent with the given source, agentID, and PKIIdentity.
     *
     * @param source   The source of the event.
     * @param agentID  The ID of the agent associated with the new connection.
     * @param identity The PKIIdentity of the agent associated with the new connection, if available.
     */
    public ConnectionAddedEvent(Object source, String agentID, PKIIdentity identity)
    {
        super(source);
        this.agentID = agentID;
        this.identity = identity;
    }


    /**
     * Returns the ID of the agent associated with the new connection.
     *
     * @return The ID of the agent associated with the new connection.
     */
    public String getAgentID()
    {
        return agentID;
    }


    /**
     * Returns an Optional containing the PKIIdentity of the agent associated with the new connection, if available.
     *
     * @return An Optional containing the PKIIdentity of the agent associated with the new connection, if available.
     */
    public Optional<PKIIdentity> getPkiIdentity()
    {
        return Optional.ofNullable(identity);
    }
}
