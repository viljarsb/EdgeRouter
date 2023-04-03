package com.mms.EdgeRouter.ConnectionManagement.Events;


import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.context.ApplicationEvent;

import java.util.Optional;

/**
 * An event that is published when a WebSocket connection is removed from the map of active connections in the ConnectionRepository class.
 * The event contains information about the removed connection, including the AgentID and PKIIdentity.
 */
public class ConnectionRemovedEvent extends ApplicationEvent
{
    private String AgentID;
    private PKIIdentity pkiIdentity;


    /**
     * Constructs a new ConnectionRemovedEvent with the given source, AgentID, and PKIIdentity.
     *
     * @param source      The source of the event.
     * @param AgentID     The ID of the agent associated with the removed connection.
     * @param pkiIdentity The PKIIdentity of the agent associated with the removed connection, if available.
     */
    public ConnectionRemovedEvent(Object source, String AgentID, PKIIdentity pkiIdentity)
    {
        super(source);
        this.AgentID = AgentID;
        this.pkiIdentity = pkiIdentity;
    }


    /**
     * Returns the ID of the agent associated with the removed connection.
     *
     * @return The ID of the agent associated with the removed connection.
     */
    public String getAgentID()
    {
        return AgentID;
    }


    /**
     * Returns an Optional containing the PKIIdentity of the agent associated with the removed connection, if available.
     *
     * @return An Optional containing the PKIIdentity of the agent associated with the removed connection, if available.
     */
    public Optional<PKIIdentity> getPkiIdentity()
    {
        return Optional.ofNullable(pkiIdentity);
    }

}