package com.mms.EdgeRouter.MessageRelay.Events;

import Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;
import java.util.List;

public class RemoteForwardRequestDirected extends ApplicationEvent
{
    private final DirectApplicationMessage message;

    public RemoteForwardRequestDirected(Object source, DirectApplicationMessage message)
    {
        super(source);
        this.message = message;
    }


    public ByteBuffer getBuffer()
    {
        return message.toByteString().asReadOnlyByteBuffer();
    }


    public List<String> getRecipients()
    {
        return message.getRecipientsList();
    }

    public DirectApplicationMessage getMessage()
    {
        return message;
    }
}
