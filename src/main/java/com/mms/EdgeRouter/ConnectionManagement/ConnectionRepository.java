package com.mms.EdgeRouter.ConnectionManagement;

import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionAddedEvent;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionRemovedEvent;
import com.mms.EdgeRouter.WebSocket.SessionEstablishedEvent;
import com.mms.EdgeRouter.WebSocket.SessionTerminatedEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class ConnectionRepository implements IConnectionRepository
{
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, ClientConnectionContext> connections = new ConcurrentHashMap<>();


    @Autowired
    public ConnectionRepository(@NonNull ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }


    @EventListener
    public void onWebSocketSessionEstablished(SessionEstablishedEvent event)
    {
        WebSocketSession session = event.getSession();
        ClientConnectionContext context = new ClientConnectionContext(session);
        connections.put(session.getId(), context);

        ConnectionAddedEvent connectionAddedEvent = new ConnectionAddedEvent(this, context.getAgentID(), context.getIdentity());
        eventPublisher.publishEvent(connectionAddedEvent);
    }


    @EventListener
    public void onWebSocketSessionTerminated(SessionTerminatedEvent event)
    {
        WebSocketSession session = event.getSession();
        ClientConnectionContext context = connections.remove(session.getId());

        ConnectionRemovedEvent connectionRemovedEvent = new ConnectionRemovedEvent(this, context.getAgentID());
        eventPublisher.publishEvent(connectionRemovedEvent);
    }


    @Override
    public void closeConnection(@NonNull String AgentID, int statusCode, String reason)
    {
        ClientConnectionContext context = connections.remove(AgentID);
        if (context != null)
        {
            CloseStatus closeStatus = new CloseStatus(statusCode, reason);

            try
            {
                context.getSession().close(closeStatus);
            }

            catch (IOException ex)
            {
                log.error("Failed to close connection for agent: {}", AgentID, ex);
            }
        }
    }


    @Override
    public void closeAllConnections(int statusCode, String reason)
    {
        for (Map.Entry<String, ClientConnectionContext> entry : connections.entrySet())
        {
            closeConnection(entry.getKey(), statusCode, reason);
        }
    }


    @Override
    public Optional<PKIIdentity> getIdentity(@NonNull String agentID)
    {
        ClientConnectionContext context = connections.get(agentID);
        PKIIdentity identity = context.getIdentity();

        if (identity != null)
        {
            return Optional.of(identity);
        }

        return Optional.empty();
    }


    @Override
    public Optional<WebSocketSession> getSession(@NonNull String AgentID)
    {
        ClientConnectionContext context = connections.get(AgentID);

        if (context != null)
        {
            return Optional.of(context.getSession());
        }

        return Optional.empty();
    }


    @Override
    public int getConnectionCount()
    {
        return connections.size();
    }


    @Override
    public List<ClientConnectionContext> getAllConnections()
    {
        return List.copyOf(connections.values());
    }
}
