package com.mms.EdgeRouter.MessageRelay.Events;

import Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;

/**
 * An event used to request the forwarding of a SubjectCastApplicationMessage locally.
 */
public final class LocalSubjectForwardRequest extends ApplicationEvent
{
    private final SubjectCastApplicationMessage message;


    /**
     * Constructs a new LocalSubjectForwardRequest with the given dependencies.
     *
     * @param source  The source of the event.
     * @param message The SubjectCastApplicationMessage to forward.
     */
    public LocalSubjectForwardRequest(Object source, SubjectCastApplicationMessage message)
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
     * Returns the message as a read-only byte buffer.
     *
     * @return The message as a read-only byte buffer.
     */
    public ByteBuffer getBuffer()
    {
        return message.toByteString().asReadOnlyByteBuffer();
    }

    public SubjectCastApplicationMessage getMessage()
    {
        return message;
    }
}
