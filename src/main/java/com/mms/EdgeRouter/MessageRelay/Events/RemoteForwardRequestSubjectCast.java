package com.mms.EdgeRouter.MessageRelay.Events;

import Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import org.springframework.context.ApplicationEvent;

import java.nio.ByteBuffer;

public class RemoteForwardRequestSubjectCast extends ApplicationEvent
{
    private final SubjectCastApplicationMessage message;

    public RemoteForwardRequestSubjectCast(Object source, SubjectCastApplicationMessage message)
    {
        super(source);
        this.message = message;
    }

    public ByteBuffer getBuffer()
    {
        return message.toByteString().asReadOnlyByteBuffer();
    }

    public String getSubject()
    {
        return message.getSubject();
    }

    public SubjectCastApplicationMessage getMessage()
    {
        return message;
    }
}
