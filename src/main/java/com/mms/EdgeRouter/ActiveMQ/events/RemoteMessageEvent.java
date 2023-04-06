package com.mms.EdgeRouter.ActiveMQ.events;

import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;

/**
 * The `RemoteMessageEvent` class represents an event that is published when a ActiveMQ/other remote  message is received.
 * It contains a reference to the message payload.
 */
public class RemoteMessageEvent extends ApplicationEvent
{
    private final ByteBuffer buffer;


    /**
     * Constructs a new `{@link RemoteMessageEvent}` with the given source object and message payload.
     *
     * @param source The object that is the source of the event.
     * @param buffer The ByteBuffer containing the message payload.
     */
    public RemoteMessageEvent(Object source, ByteBuffer buffer)
    {
        super(source);
        this.buffer = buffer;
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
}
