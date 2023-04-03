package com.mms.EdgeRouter.ConnectionManagement;

import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionAddedEvent;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionCloseRequest;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionRemovedEvent;
import com.mms.EdgeRouter.WebSocket.Events.SessionEstablishedEvent;
import com.mms.EdgeRouter.WebSocket.Events.SessionTerminatedEvent;
import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConnectionRepository manages WebSocket connections between the Edge Router and agents.
 * It uses an event-driven model to handle the establishment and termination of WebSocket sessions.
 * It maintains a map of ClientConnectionContext objects, each representing a single WebSocket session.
 * This class provides methods to add, remove, and close WebSocket connections, as well as access to
 * the number of active connections and a list of all active connections.
 */
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


    /**
     * Asynchronously handles the event when a WebSocket session is established.
     * Adds the new connection to the map of active connections.
     * Publishes a ConnectionAddedEvent to notify listeners of the new connection.
     *
     * @param event The SessionEstablishedEvent.
     */
    @Async("ConnectionPool")
    @EventListener
    public void onWebSocketSessionEstablished(@NonNull SessionEstablishedEvent event)
    {
        WebSocketSession session = event.getSession();
        addConnection(session);
    }


    /**
     * Asynchronously handles the event when a WebSocket session is terminated.
     * Removes the terminated connection from the map of active connections.
     * Publishes a ConnectionRemovedEvent to notify listeners of the terminated connection.
     *
     * @param event The SessionTerminatedEvent.
     */
    @Async("ConnectionPool")
    @EventListener
    public void onWebSocketSessionTerminated(@NonNull SessionTerminatedEvent event)
    {
        WebSocketSession session = event.getSession();
        removeConnection(session);
    }


    /**
     * Asynchronously handles a ConnectionCloseRequest event by extracting the agentID, closeCode, and closeReason from the request and passing them to the closeConnection() method to close the WebSocket connection with the given agentID using the specified closeCode and closeReason.
     *
     * @param request The ConnectionCloseRequest object containing information about the WebSocket connection to be closed.
     */
    @Async("ConnectionPool")
    @EventListener
    public void onSessionCloseRequest(@NonNull ConnectionCloseRequest request)
    {
        String agentID = request.getAgentID();
        int closeCode = request.getCloseCode();
        String closeReason = request.getCloseReason();
        closeConnection(agentID, closeCode, closeReason);
    }


    /**
     * Asynchronously adds a new connection to the map of active connections.
     * If the connection is authenticated, logs information about the authenticated agent.
     * Publishes a ConnectionAddedEvent to notify listeners of the new connection.
     *
     * @param session The WebSocketSession to add.
     */
    @Async("ConnectionPool")
    protected void addConnection(@NonNull WebSocketSession session)
    {
        ClientConnectionContext context = new ClientConnectionContext(session);
        connections.put(session.getId(), context);

        if (context.getIdentity() != null)
        {
            log.info("Authenticated agent added: agentId={}, commonName={}, mrn={}", context.getAgentID(), context.getIdentity().getCn(), context.getIdentity().getMrn());
        }
        else
        {
            log.info("Unauthenticated agent added: agentId={}", context.getAgentID());
        }

        ConnectionAddedEvent connectionAddedEvent = new ConnectionAddedEvent(this, context.getAgentID(), context.getIdentity());
        eventPublisher.publishEvent(connectionAddedEvent);
    }


    /**
     * Asynchronously removes a connection from the map of active connections.
     * If the connection was successfully removed, logs information about the terminated connection.
     * Publishes a ConnectionRemovedEvent to notify listeners of the terminated connection.
     *
     * @param session The WebSocketSession to remove.
     */
    @Async("ConnectionPool")
    protected void removeConnection(@NonNull WebSocketSession session)
    {
        ClientConnectionContext context = connections.remove(session.getId());
        if (context != null)
        {
            log.info("Connection removed: agentId={}", context.getAgentID());
            ConnectionRemovedEvent connectionRemovedEvent = new ConnectionRemovedEvent(this, context.getAgentID(), context.getIdentity());
            eventPublisher.publishEvent(connectionRemovedEvent);
        }
    }


    /**
     * Asynchronously closes a WebSocket connection with a given agent ID.
     * If the connection is successfully closed, logs information about the closed connection.
     *
     * @param agentID    The ID of the agent whose connection to close.
     * @param statusCode The status code to send when closing the connection.
     * @param reason     The reason to send when closing the connection.
     */
    @Async("ConnectionPool")
    protected void closeConnection(@NonNull String agentID, int statusCode, @NonNull String reason)
    {
        ClientConnectionContext context = connections.remove(agentID);
        if (context != null)
        {
            CloseStatus closeStatus = new CloseStatus(statusCode, reason);

            try
            {
                context.getSession().close(closeStatus);
                log.info("Connection closed: agent={}, statusCode={}, reason={}", agentID, statusCode, reason);
            }

            catch (IOException ex)
            {
                log.error("Failed to close connection for agent: {}", agentID, ex);
            }
        }

        else
        {
            log.warn("Attempted to close non-existent connection for agent: {}", agentID);
        }
    }


    /**
     * Asynchronously closes all WebSocket connections.
     * Logs information about the number of connections closed.
     *
     * @param statusCode The status code to send when closing the connections.
     * @param reason     The reason to send when closing the connections.
     */
    @Async("ConnectionPool")
    protected void closeAllConnections(int statusCode, String reason)
    {
        log.info("Closing all connections: statusCode={}, reason={}", statusCode, reason);
        for (Map.Entry<String, ClientConnectionContext> entry : connections.entrySet())
        {
            closeConnection(entry.getKey(), statusCode, reason);
        }
    }


    /**
     * Returns the PKIIdentity associated with a given agent ID, if any.
     *
     * @param agentID The ID of the agent to get the PKIIdentity for.
     * @return An Optional containing the PKIIdentity, if one is associated with the agent ID.
     */
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


    /**
     * Returns the WebSocketSession associated with a given agent ID, if any.
     *
     * @param agentID The ID of the agent to get the WebSocketSession for.
     * @return An Optional containing the WebSocketSession, if one is associated with the agent ID.
     */

    @Override
    public Optional<WebSocketSession> getSession(@NonNull String agentID)
    {
        ClientConnectionContext context = connections.get(agentID);

        if (context != null)
        {
            return Optional.of(context.getSession());
        }

        return Optional.empty();
    }


    /**
     * Returns a list of WebSocketSessions associated with the given list of agent IDs.
     *
     * @param agentIDs The list of agent IDs to get the WebSocketSessions for.
     * @return A list of WebSocketSessions associated with the given list of agent IDs.
     */
    @Override
    public List<WebSocketSession> getSessions(@NonNull List<String> agentIDs)
    {
        return agentIDs.stream().map(this::getSession).filter(Optional::isPresent).map(Optional::get).toList();
    }


    /**
     * Returns the number of active connections.
     *
     * @return The number of active connections.
     */
    @Override
    public int getConnectionCount()
    {
        return connections.size();
    }


    /**
     * Returns a list of all active connections.
     *
     * @return A list of all active connections.
     */
    @Override
    public List<ClientConnectionContext> getAllConnections()
    {
        return List.copyOf(connections.values());
    }


    /**
     * Closes all WebSocket connections before the object is destroyed.
     */
    @PreDestroy
    public void destroy()
    {
        closeAllConnections(CloseStatus.GOING_AWAY.getCode(), "Edge Router is shutting down");
    }
}
