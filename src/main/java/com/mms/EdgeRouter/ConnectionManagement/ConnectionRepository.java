package com.mms.EdgeRouter.ConnectionManagement;

import com.mms.EdgeRouter.ConnectionManagement.ClientContext.AnonymousClientContext;
import com.mms.EdgeRouter.ConnectionManagement.ClientContext.AuthenticatedClientContext;
import com.mms.EdgeRouter.ConnectionManagement.ClientContext.ClientConnectionContext;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionAddedEvent;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionCloseRequest;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionRemovedEvent;
import com.mms.EdgeRouter.WebSocket.Events.SessionEstablishedEvent;
import com.mms.EdgeRouter.WebSocket.Events.SessionTerminatedEvent;
import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.maritimeconnectivity.pki.CertificateHandler;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;


import java.io.IOException;
import java.security.cert.X509Certificate;
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
    private final Map<String, ClientConnectionContext> contexts = new ConcurrentHashMap<>();


    @Autowired
    public ConnectionRepository(@NonNull ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }


    /**
     * Asynchronously handles the event when a WebSocket session is established.
     * Adds the new connection to the map of active connections.
     * Publishes a {@link ConnectionAddedEvent} to notify listeners of the new connection.
     *
     * @param event The {@link SessionEstablishedEvent}.
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
     * Publishes a {@link ConnectionRemovedEvent} to notify listeners of the terminated connection.
     *
     * @param event The {@link SessionTerminatedEvent}.
     */
    @Async("ConnectionPool")
    @EventListener
    public void onWebSocketSessionTerminated(@NonNull SessionTerminatedEvent event)
    {
        WebSocketSession session = event.getSession();
        removeConnection(session);
    }


    /**
     * Asynchronously handles a {@link ConnectionCloseRequest} event by extracting the agentID, closeCode, and closeReason
     * from the request and passing them to the {@link #closeConnection(String, int, String)} method to close the WebSocket connection
     * with the given agentID using the specified closeCode and closeReason.
     *
     * @param request The {@link ConnectionCloseRequest} object containing information about the WebSocket connection to be closed.
     */
    @Async("ConnectionPool")
    @EventListener
    public void onSessionCloseRequest(@NonNull ConnectionCloseRequest request)
    {
        String agentID = request.getAgentID();
        int closeCode = request.getCloseCode();
        String closeReason = request.getClosePhrase();
        closeConnection(agentID, closeCode, closeReason);
    }


    /**
     * Called in response to a {@link SessionEstablishedEvent}.
     * Asynchronously adds a new connection to the map of active connections.
     * Publishes a {@link ConnectionAddedEvent} to notify listeners of the new connection.
     *
     * @param session The {@link WebSocketSession} that has been established.
     */
    @Async("ConnectionPool")
    protected void addConnection(@NonNull WebSocketSession session)
    {
        X509Certificate certificate = (X509Certificate) session.getAttributes().get("MMS-CERTIFICATE");

        if (certificate != null)
        {
            PKIIdentity identity = CertificateHandler.getIdentityFromCert(certificate);
            log.info("Authenticated agent added: agent={}, commonName={}, mrn={}", session.getId(), identity.getCn(), identity.getMrn());
            AuthenticatedClientContext context = new AuthenticatedClientContext(session, identity.getMrn());
            contexts.put(session.getId(), context);

            ConnectionAddedEvent connectionAddedEvent = new ConnectionAddedEvent(this, context.getAgentID(), context.getMRN());
            eventPublisher.publishEvent(connectionAddedEvent);
        }

        else
        {
            log.info("Unauthenticated agent added: agent={}", session.getId());
            AnonymousClientContext context = new AnonymousClientContext(session);
            contexts.put(session.getId(), context);

            ConnectionAddedEvent connectionAddedEvent = new ConnectionAddedEvent(this, context.getAgentID());
            eventPublisher.publishEvent(connectionAddedEvent);
        }
    }


    /**
     * Called in response to a {@link SessionTerminatedEvent}.
     * Asynchronously removes the terminated connection from the map of active connections.
     * Publishes a {@link ConnectionRemovedEvent} to notify listeners of the terminated connection.
     *
     * @param session The {@link WebSocketSession} to remove.
     */
    @Async("ConnectionPool")
    protected void removeConnection(@NonNull WebSocketSession session)
    {
        ClientConnectionContext context = contexts.remove(session.getId());

        if (context != null)
        {
            if (context instanceof AuthenticatedClientContext authenticatedContext)
            {
                log.info("Authenticated agent removed: agent={}, mrn={}", authenticatedContext.getAgentID(), authenticatedContext.getMRN());
                ConnectionRemovedEvent connectionRemovedEvent = new ConnectionRemovedEvent(this, authenticatedContext.getAgentID(), authenticatedContext.getMRN());
                eventPublisher.publishEvent(connectionRemovedEvent);
            }

            else if (context instanceof AnonymousClientContext anonymousContext)
            {
                log.info("Unauthenticated agent removed: agent={}", anonymousContext.getAgentID());
                ConnectionRemovedEvent connectionRemovedEvent = new ConnectionRemovedEvent(this, anonymousContext.getAgentID());
                eventPublisher.publishEvent(connectionRemovedEvent);
            }
        }
    }


    /**
     * Asynchronously closes a WebSocket connection with a given agent ID.
     *
     * @param agentID    The ID of the agent whose connection to close.
     * @param statusCode The status code to send when closing the connection.
     * @param reason     The reason to send when closing the connection.
     */
    @Async("ConnectionPool")
    protected void closeConnection(@NonNull String agentID, int statusCode, @NonNull String reason)
    {
        ClientConnectionContext context = contexts.remove(agentID);

        if (context != null)
        {
            CloseStatus closeStatus = new CloseStatus(statusCode, reason);

            try
            {
                WebSocketSession session = context.getSession();
                session.close(closeStatus);
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
     *
     * @param statusCode The status code to send when closing the connections.
     */
    @Async("ConnectionPool")
    protected void closeAllConnections(int statusCode)
    {
        log.info("Closing all connections: statusCode={}, reason={}", statusCode, "Edge Router is shutting down");
        for (Map.Entry<String,ClientConnectionContext> entry : contexts.entrySet())
        {
            closeConnection(entry.getKey(), statusCode, "Edge Router is shutting down");
        }
    }


    /**
     * Returns the Maritime Resource Name associated with a given agent ID, if any.
     *
     * @param agentID The ID of the agent to get the MRN for.
     * @return An Optional containing the MRN, if one is associated with the agent ID.
     */
    @Override
    public Optional<String> getMRN(@NonNull String agentID)
    {
        ClientConnectionContext context = contexts.get(agentID);

        if (context instanceof AuthenticatedClientContext authenticatedContext)
        {
            return Optional.of(authenticatedContext.getMRN());
        }

        return Optional.empty();
    }


    /**
     * Returns the WebSocket session associated with a given agent ID, if any.
     *
     * @param agentID The ID of the agent to get the {@link WebSocketSession} for.
     * @return An Optional containing the {@link WebSocketSession}, if one is associated with the agent ID.
     */
    @Override
    public Optional<WebSocketSession> getSession(@NonNull String agentID)
    {
        ClientConnectionContext context = contexts.get(agentID);

        if (context != null)
        {
            return Optional.of(context.getSession());
        }

        return Optional.empty();
    }


    /**
     * Returns a list of WebSocket sessions associated with the given list of agent IDs.
     *
     * @param agentIDs The list of agent IDs to get the WebSocketSessions for.
     * @return A list of {@link WebSocketSession} objects associated with the given list of agent IDs.
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
        return contexts.size();
    }


    /**
     * Returns a list of all active connections.
     *
     * @return A list of all active connections.
     */
    @Override
    public List<ClientConnectionContext> getAllConnections()
    {
        return List.copyOf(contexts.values());
    }


    /**
     * Closes all WebSocket connections before the object is destroyed.
     */
    @PreDestroy
    public void destroy()
    {
        closeAllConnections(CloseStatus.GOING_AWAY.getCode());
    }
}
