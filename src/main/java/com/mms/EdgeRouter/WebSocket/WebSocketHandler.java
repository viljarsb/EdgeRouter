package com.mms.EdgeRouter.WebSocket;

import com.mms.EdgeRouter.WebSocket.SessionEstablishedEvent;
import com.mms.EdgeRouter.WebSocket.SessionTerminatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler
{
    @Autowired
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception
    {
        SessionEstablishedEvent event = new SessionEstablishedEvent(this, session);
        eventPublisher.publishEvent(event);
    }


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception
    {

    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception
    {

    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception
    {
        SessionTerminatedEvent event = new SessionTerminatedEvent(this, session);
        eventPublisher.publishEvent(event);
    }


    @Override
    public boolean supportsPartialMessages()
    {
        return false;
    }
}
