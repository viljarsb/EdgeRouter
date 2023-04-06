package com.mms.EdgeRouter.WebSocket.Events;

import lombok.NonNull;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;


/**
 * The `LocalMessageEvent` class represents an event that is published when a WebSocket message is received.
 * It contains a reference to the message payload, as well as the ID of the WebSocket session that sent the message.
 */
public class LocalMessageEvent extends ApplicationEvent
{
    private final ByteBuffer buffer;
    private final String agentID;


    /**
     * Constructs a new `{@link LocalMessageEvent}` with the given source object, message payload, and session ID.
     *
     * @param source  The object that is the source of the event.
     * @param buffer  The ByteBuffer containing the message payload.
     * @param agentID The ID of the WebSocket that sent the message.
     */
    public LocalMessageEvent(@NonNull Object source, @NonNull ByteBuffer buffer, @NonNull String agentID)
    {
        super(source);
        this.buffer = buffer;
        this.agentID = agentID;
    }


    /**
     * Returns the ByteBuffer containing the message payload.
     *
     * @return The ByteBuffer containing the message payload.
     */
    public ByteBuffer getBuffer()
    {
        return buffer;
    }


    /**
     * Returns the ID of the connection that sent the message.
     *
     * @return The ID of the WebSocket session that sent the message.
     */
    public String getAgentID()
    {
        return agentID;
    }
}
