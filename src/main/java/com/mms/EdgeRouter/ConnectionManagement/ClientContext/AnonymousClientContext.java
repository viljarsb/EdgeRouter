package com.mms.EdgeRouter.ConnectionManagement.ClientContext;

import lombok.NonNull;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

/**
 * The AnonymousClientContext class represents the context of a WebSocket connection with an agent.
 * Implements the {@link ClientConnectionContext} interface.
 */
public class AnonymousClientContext implements ClientConnectionContext
{
    private final WebSocketSession session;
    private final URI connectionURI;
    private final String remoteAddress;
    private final String agentID;


    /**
     * Constructs a new {@link AnonymousClientContext} object with the given WebSocket session.
     *
     * @param session The WebSocket session representing the connection with the agent.
     */
    public AnonymousClientContext(@NonNull WebSocketSession session)
    {
        this.session = session;
        this.connectionURI = session.getUri();
        this.remoteAddress = session.getRemoteAddress().getAddress().getHostAddress();
        this.agentID = session.getId();
    }


    /**
     * Returns the WebSocket session representing the connection with the agent.
     *
     * @return The WebSocketSession object associated with the connection.
     */
    @Override
    public WebSocketSession getSession()
    {
        return session;
    }


    /**
     * Returns the connection URI of the WebSocket connection with the agent.
     *
     * @return The URI of the WebSocket connection.
     */
    @Override
    public URI getConnectionURI()
    {
        return connectionURI;
    }


    /**
     * Returns the remote address of the WebSocket connection with the agent.
     *
     * @return The remote address of the WebSocket connection.
     */
    @Override
    public String getRemoteAddress()
    {
        return remoteAddress;
    }


    /**
     * Returns the agent ID of the WebSocket connection.
     *
     * @return The agent ID.
     */
    @Override
    public String getAgentID()
    {
        return agentID;
    }


    /**
     * Compares this {@link AnonymousClientContext} object with the specified {@link ClientConnectionContext} object for order.
     * Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     *
     * @param other The {@link ClientConnectionContext} object to compare to this object.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@NonNull ClientConnectionContext other)
    {
        return agentID.compareTo(other.getAgentID());
    }
}
