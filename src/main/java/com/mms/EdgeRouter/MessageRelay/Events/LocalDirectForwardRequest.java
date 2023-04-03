package com.mms.EdgeRouter.MessageRelay.Events;

import Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * An event used to request the forwarding of a DirectApplicationMessage locally.
 */
public final class LocalDirectForwardRequest extends ApplicationEvent
{
    private final DirectApplicationMessage message;

    /**
     * Constructs a new LocalDirectForwardRequest with the given dependencies.
     *
     * @param source  The source of the event.
     * @param message The DirectApplicationMessage to forward.
     */
    public LocalDirectForwardRequest(Object source, DirectApplicationMessage message)
    {
        super(source);
        this.message = message;
    }

    /**
     * Returns the list of recipients for the message.
     *
     * @return The list of recipients for the message.
     */
    public List<String> getRecipients()
    {
        return message.getRecipientsList();
    }

    /**
     * Returns the message as a read-only byte buffer.
     *
     * @return The message as a read-only byte buffer.
     */
    public ByteBuffer getBuffer()
    {
        return message.toByteString().asReadOnlyByteBuffer();
    }

    public DirectApplicationMessage getMessage()
    {
        return message;
    }
}
