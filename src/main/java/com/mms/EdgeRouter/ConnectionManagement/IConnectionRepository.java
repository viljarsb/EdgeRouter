package com.mms.EdgeRouter.ConnectionManagement;

import com.mms.EdgeRouter.ConnectionManagement.ClientContext.ClientConnectionContext;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

/**
 * Contract for a repository that manages the connections from the WebSocket clients.
 */
public interface IConnectionRepository
{
    Optional<String> getMRN(String agentID);

    Optional<WebSocketSession> getSession(String agentID);

    List<WebSocketSession> getSessions(List<String> agentIDs);

    int getConnectionCount();

    List<ClientConnectionContext> getAllConnections();
}
