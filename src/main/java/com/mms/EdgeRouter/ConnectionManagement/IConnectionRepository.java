package com.mms.EdgeRouter.ConnectionManagement;


import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

public interface IConnectionRepository
{
    void closeConnection(String AgentID, int statusCode, String reason);
    void closeAllConnections(int statusCode, String reason);
    Optional<PKIIdentity> getIdentity(String AgentID);
    Optional<WebSocketSession> getSession(String AgentID);
    int getConnectionCount();
    List<ClientConnectionContext> getAllConnections();
}
