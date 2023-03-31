package com.mms.EdgeRouter.WebSocket;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

public class SessionEstablishedEvent extends ApplicationEvent
{
    private final WebSocketSession session;

    public SessionEstablishedEvent(Object source, WebSocketSession session)
    {
        super(source);
        this.session = session;
    }

    public WebSocketSession getSession()
    {
        return session;
    }
}
