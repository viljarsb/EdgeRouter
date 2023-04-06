package com.mms.EdgeRouter.MessageRelay.Events;

import MMTPMessageFormats.DirectApplicationMessage;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * An event used to request the forwarding of a {@link DirectApplicationMessage} locally.
 */
public final class LocalDirectMessageForwardRequest extends ApplicationEvent
{
    private final DirectApplicationMessage message;

    /**
     * Constructs a new {@link LocalDirectMessageForwardRequest}.
     *
     * @param source  The source of the event.
     * @param message The DirectApplicationMessage to forward.
     */
    public LocalDirectMessageForwardRequest(Object source, DirectApplicationMessage message)
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
     * Returns the message as a ByteBuffer.
     *
     * @return The message as a ByteBuffer.
     */
    public ByteBuffer getBuffer()
    {
        return ByteBuffer.wrap(message.toByteArray());
    }


    /**
     * Returns the message.
     *
     * @return  The message.
     */
    public DirectApplicationMessage getMessage()
    {
        return message;
    }
}
