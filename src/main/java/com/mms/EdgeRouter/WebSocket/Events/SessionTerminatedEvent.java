package com.mms.EdgeRouter.WebSocket.Events;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

/**
 * The `{@link SessionTerminatedEvent}` class represents an event that is published when a {@link WebSocketSession} session is terminated.
 * It contains a reference to the terminated `{@link WebSocketSession}`.
 */
public class SessionTerminatedEvent extends ApplicationEvent
{
    private final WebSocketSession session;


    /**
     * Constructs a new `{@link SessionTerminatedEvent}` with the given source object and `{@link WebSocketSession}`.
     *
     * @param source  The object that is the source of the event.
     * @param session The terminated `WebSocketSession`.
     */
    public SessionTerminatedEvent(Object source, WebSocketSession session)
    {
        super(source);
        this.session = session;
    }


    /**
     * Returns the terminated `{@link WebSocketSession}`.
     *
     * @return The terminated `{@link WebSocketSession}`.
     */
    public WebSocketSession getSession()
    {
        return session;
    }
}
