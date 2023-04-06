package com.mms.EdgeRouter.WebSocket.Events;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

/**
 * The `SessionEstablishedEvent` class represents an event that is published when a {@link WebSocketSession} is established.
 * It contains a reference to the established `{@link WebSocketSession}`.
 */
public class SessionEstablishedEvent extends ApplicationEvent
{
    private final WebSocketSession session;


    /**
     * Constructs a new `{@link SessionEstablishedEvent}` with the given source object and `{@link WebSocketSession}`.
     *
     * @param source  The object that is the source of the event.
     * @param session The established `WebSocketSession`.
     */
    public SessionEstablishedEvent(Object source, WebSocketSession session)
    {
        super(source);
        this.session = session;
    }


    /**
     * Returns the established `{@link WebSocketSession}`.
     *
     * @return The established `WebSocketSession`.
     */
    public WebSocketSession getSession()
    {
        return session;
    }
}
