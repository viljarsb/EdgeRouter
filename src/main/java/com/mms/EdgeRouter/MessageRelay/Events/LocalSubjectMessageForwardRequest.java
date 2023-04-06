package com.mms.EdgeRouter.MessageRelay.Events;

import Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;

/**
 * An event used to request the forwarding of a {@link SubjectCastApplicationMessage} locally.
 */
public final class LocalSubjectMessageForwardRequest extends ApplicationEvent
{
    private final SubjectCastApplicationMessage message;


    /**
     * Constructs a new {@link LocalSubjectMessageForwardRequest}.
     *
     * @param source  The source of the event.
     * @param message The SubjectCastApplicationMessage to forward.
     */
    public LocalSubjectMessageForwardRequest(Object source, SubjectCastApplicationMessage message)
    {
        super(source);
        this.message = message;
    }


    /**
     * Returns the subject of the message.
     *
     * @return The subject of the message.
     */
    public String getSubject()
    {
        return message.getSubject();
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
     * @return The message.
     */
    public SubjectCastApplicationMessage getMessage()
    {
        return message;
    }
}
