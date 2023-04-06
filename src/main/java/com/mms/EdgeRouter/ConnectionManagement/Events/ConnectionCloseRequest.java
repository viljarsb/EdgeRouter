package com.mms.EdgeRouter.ConnectionManagement.Events;

import org.springframework.context.ApplicationEvent;

/**
 * An event that requests the closure of a WebSocket connection with a given agentID, closeCode, and closeReason.
 */
public class ConnectionCloseRequest extends ApplicationEvent
{
    private final String agentID;
    private final int closeCode;
    private final String closePhrase;


    /**
     * Constructs a new ConnectionCloseRequest event with the given source, agentID, closeCode, and closeReason.
     *
     * @param source      The source of the event.
     * @param agentID     The ID of the agent whose connection is to be closed.
     * @param closeCode   The status code to send when closing the connection.
     * @param closeReason The reason to send when closing the connection.
     */
    public ConnectionCloseRequest(Object source, String agentID, int closeCode, String closeReason)
    {
        super(source);
        this.agentID = agentID;
        this.closeCode = closeCode;
        this.closePhrase = closeReason;
    }


    /**
     * Returns the ID of the agent whose connection is to be closed.
     *
     * @return The ID of the agent whose connection is to be closed.
     */
    public String getAgentID()
    {
        return agentID;
    }


    /**
     * Returns the status code to send when closing the connection.
     *
     * @return The status code to send when closing the connection.
     */
    public int getCloseCode()
    {
        return closeCode;
    }


    /**
     * Returns the reason to send when closing the connection.
     *
     * @return The reason to send when closing the connection.
     */
    public String getClosePhrase()
    {
        return closePhrase;
    }
}
