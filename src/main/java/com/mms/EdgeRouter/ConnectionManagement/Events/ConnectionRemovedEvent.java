package com.mms.EdgeRouter.ConnectionManagement.Events;


import org.springframework.context.ApplicationEvent;

import java.util.Optional;

/**
 * An event that is published when a WebSocket connection is removed from the map of active connections in the ConnectionRepository class.
 * The event contains information about the removed connection, including the AgentID and Maritime Resource Name (if authenticated).
 */
public class ConnectionRemovedEvent extends ApplicationEvent
{
    private final String agentID;
    private final String maritimeResourceName;


    /**
     * Constructs a new ConnectionRemovedEvent with the given source, AgentID, and Maritime Resource Name.
     *
     * @param source               The source of the event.
     * @param agentID              The ID of the agent associated with the removed connection.
     * @param maritimeResourceName The MRN of the agent associated with the removed connection, if available.
     */
    public ConnectionRemovedEvent(Object source, String agentID, String maritimeResourceName)
    {
        super(source);
        this.agentID = agentID;
        this.maritimeResourceName = maritimeResourceName;
    }


    /**
     * Constructs a new ConnectionRemovedEvent with the given source and AgentID.
     *
     * @param source  The source of the event.
     * @param AgentID The ID of the agent associated with the removed connection.
     */
    public ConnectionRemovedEvent(Object source, String AgentID)
    {
        super(source);
        this.agentID = AgentID;
        this.maritimeResourceName = null;
    }


    /**
     * Returns the ID of the agent associated with the removed connection.
     *
     * @return The ID of the agent associated with the removed connection.
     */
    public String getAgentID()
    {
        return agentID;
    }


    /**
     * Returns an Optional containing the Maritime Resource Name of the agent associated with the removed connection, if available.
     *
     * @return An Optional containing the Maritime Resource Name of the agent associated with the removed connection, if available.
     */
    public Optional<String> getMRN()
    {
        return Optional.ofNullable(maritimeResourceName);
    }

}