package com.mms.EdgeRouter.WebSocket;

import com.mms.EdgeRouter.WebSocket.Events.LocalMessageEvent;
import com.mms.EdgeRouter.WebSocket.Events.SessionEstablishedEvent;
import com.mms.EdgeRouter.WebSocket.Events.SessionTerminatedEvent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;

/**
 * WsHandler is a Spring WebSocket handler that manages WebSocket connections to Edge Router.
 * It is responsible for handling incoming WebSocket messages and events, and publishing related events to other
 * components of the system. It also handles rate limiting and IP blocking.
 */
@Slf4j
@Service
public class WsHandler extends AbstractWebSocketHandler
{
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Constructs a new {@link WsHandler} with the given ApplicationEventPublisher.
     *
     * @param eventPublisher The ApplicationEventPublisher used to publish events related to WebSocket connections.
     */
    @Autowired
    public WsHandler(@NonNull ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        log.info("WsHandler initialized");
    }


    /**
     * Called when a new {@link WebSocketSession} connection is established.
     *
     * @param session The WebSocketSession representing the new connection.
     */
    @Async("ConnectionPool")
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session)
    {
        SessionEstablishedEvent event = new SessionEstablishedEvent(this, session);
        eventPublisher.publishEvent(event);
        log.info("New WebSocket connection established: sessionId={}, remoteAddress={}.", session.getId(), session.getRemoteAddress());
    }


    /**
     * Handles a {@link BinaryMessage} WebSocket message.
     *
     * @param session The WebSocketSession representing the connection.
     * @param message The BinaryMessage containing the message payload.
     */
    @Async("WorkerPool")
    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message)
    {
        LocalMessageEvent event = new LocalMessageEvent(this, message.getPayload(), session.getId());
        eventPublisher.publishEvent(event);
        log.debug("Received binary message from WebSocket connection: sessionId={}, messageSize={} bytes.", session.getId(), message.getPayload().limit());
    }


    /**
     * Called when a {@link WebSocketSession} connection is closed.
     *
     * @param session     The WebSocketSession representing the closed connection.
     * @param closeStatus The CloseStatus representing the reason for the closure.
     */
    @Async("ConnectionPool")
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus)
    {
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress().toString();
        log.info("WebSocket connection closed: sessionId={}, remoteAddress={}, closeStatus={}.", sessionId, remoteAddress, closeStatus);
        SessionTerminatedEvent event = new SessionTerminatedEvent(this, session);
        eventPublisher.publishEvent(event);
    }


    /**
     * Called when a transport error occurs on the {@link WebSocketSession} connection.
     *
     * @param session   The WebSocketSession representing the connection.
     * @param exception The Throwable representing the transport error.
     */
    @Async("ConnectionPool")
    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception)
    {
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress().toString();
        log.error("Transport error occurred for WebSocket connection: sessionId={}, remoteAddress={}.", sessionId, remoteAddress, exception);
        CloseStatus closeStatus = new CloseStatus(CloseStatus.SERVER_ERROR.getCode(), "Transport error occurred");
        try
        {
            session.close(closeStatus);
        }
        catch (IOException ex)
        {
            log.error("Failed to close WebSocket connection: sessionId={}, remoteAddress={}, error={}.", sessionId, remoteAddress, ex.getMessage(), ex);
        }
    }
}

