package com.mms.EdgeRouter.ActiveMQ.events;

import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;

public class RemoteMessageEvent extends ApplicationEvent
{
    private final ByteBuffer buffer;

    public RemoteMessageEvent(Object source, ByteBuffer buffer)
    {
        super(source);
        this.buffer = buffer;
    }

    public ByteBuffer getBuffer()
    {
        return buffer;
    }
}
