package com.mms.EdgeRouter.ConnectionManagement.Events;

import org.springframework.context.ApplicationEvent;

import java.util.Optional;


/**
 * An event that is published when a WebSocket connection is added to the map of active connections in the ConnectionRepository class.
 * The event contains information about the new connection, including the agentID and Maritime Resource Name (if authenticated).
 */
public class ConnectionAddedEvent extends ApplicationEvent
{
    private final String agentID;
    private final String maritimeResourceName;


    /**
     * Constructs a new ConnectionAddedEvent with the given source, agentID, and Maritime Resource Name.
     *
     * @param source               The source of the event.
     * @param agentID              The ID of the agent associated with the new connection.
     * @param maritimeResourceName The MRN of the agent associated with the new connection, if available.
     */
    public ConnectionAddedEvent(Object source, String agentID, String maritimeResourceName)
    {
        super(source);
        this.agentID = agentID;
        this.maritimeResourceName = maritimeResourceName;
    }


    /**
     * Constructs a new ConnectionAddedEvent with the given source and agentID.
     *
     * @param source  The source of the event.
     * @param agentID The ID of the agent associated with the new connection.
     */
    public ConnectionAddedEvent(Object source, String agentID)
    {
        super(source);
        this.agentID = agentID;
        this.maritimeResourceName = null;
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
     * Returns an Optional containing the Maritime Resource Name of the agent associated with the new connection, if available.
     *
     * @return An Optional containing the Maritime Resource Name of the agent associated with the new connection, if available.
     */
    public Optional<String> getMRN()
    {
        return Optional.ofNullable(maritimeResourceName);
    }
}
