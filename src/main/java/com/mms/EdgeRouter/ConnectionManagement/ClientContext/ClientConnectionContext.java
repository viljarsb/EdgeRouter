package com.mms.EdgeRouter.ConnectionManagement.ClientContext;

import org.springframework.web.socket.WebSocketSession;

import java.net.URI;

/**
 * The ClientConnectionContext interface represents the context of a WebSocket connection with an agent.
 */
public interface ClientConnectionContext extends Comparable<ClientConnectionContext>
{
    WebSocketSession getSession();

    URI getConnectionURI();

    String getRemoteAddress();

    String getAgentID();
}
