package com.mms.EdgeRouter.MessageHandlers.Remote;

import Misc.MMTPValidationException;
import MMTPMessageFormats.DirectApplicationMessage;
import MMTPMessageFormats.MessageType;
import MMTPMessageFormats.ProtocolMessage;
import MMTPMessageFormats.SubjectCastApplicationMessage;
import Misc.MMTPValidator;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mms.EdgeRouter.MessageRelay.Events.LocalDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.LocalSubjectMessageForwardRequest;
import com.mms.EdgeRouter.ActiveMQ.events.RemoteMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

/**
 * Service responsible for handling remote messages received from other brokers.
 * Implements {@link IRemoteMessageHandler} to handle {@link RemoteMessageEvent}s.
 */
@Service
@Slf4j
public class RemoteMessageHandler implements IRemoteMessageHandler
{

    private final ApplicationEventPublisher eventPublisher;


    /**
     * Constructs a new {@link RemoteMessageHandler} with the given dependencies.
     *
     * @param eventPublisher The event publisher to use.
     */
    @Autowired
    public RemoteMessageHandler(ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }


    /**
     * Asynchronously handles a {@link RemoteMessageEvent}.
     *
     * @param event The RemoteMessageEvent to handle.
     */
    @Async("WorkerPool")
    @EventListener
    @Override
    public void onRemoteMessage(RemoteMessageEvent event)
    {
        ByteBuffer buffer = event.getBuffer();
        handleRemoteMessage(buffer);
    }


    /**
     * Handles a remote message by parsing it and delegating to the appropriate message processor.
     *
     * @param buffer The buffer containing the message.
     */
    @Async("WorkerPool")
    protected void handleRemoteMessage(ByteBuffer buffer)
    {
        log.info("Handling remote message");

        try
        {
            ProtocolMessage message = ProtocolMessage.parseFrom(buffer);
            MessageType type = message.getType();

            switch (type)
            {
                case DIRECT_APPLICATION_MESSAGE -> processDirectApplicationMessage(message.getContent().asReadOnlyByteBuffer());
                case SUBJECT_CAST_APPLICATION_MESSAGE -> processSubjectCastApplicationMessage(message.getContent().asReadOnlyByteBuffer());
            }
        }
        catch (InvalidProtocolBufferException ex)
        {
            log.error("Invalid protocol buffer", ex);
        }
        catch (MMTPValidationException ex)
        {
            log.error("Invalid MMTP message", ex);
        }
    }


    /**
     * Processes a direct application message by validating it and publishing a {@link LocalDirectMessageForwardRequest} event.
     *
     * @param buffer The buffer containing the direct application message.
     * @throws InvalidProtocolBufferException If the application message could not be parsed.
     * @throws MMTPValidationException        If the application message is invalid according to the MMTP specification.
     */
    @Async("WorkerPool")
    protected void processDirectApplicationMessage(ByteBuffer buffer) throws InvalidProtocolBufferException, MMTPValidationException
    {
        log.info("Processing direct application message");
        DirectApplicationMessage applicationMessage = DirectApplicationMessage.parseFrom(buffer);
        MMTPValidator.validate(applicationMessage);

        LocalDirectMessageForwardRequest forwardRequest = new LocalDirectMessageForwardRequest(this, applicationMessage);
        eventPublisher.publishEvent(forwardRequest);
    }


    /**
     * Processes a subject cast application message by validating it and publishing a {@link LocalSubjectMessageForwardRequest} event.
     *
     * @param buffer The buffer containing the subject cast application message.
     * @throws InvalidProtocolBufferException If the application message could not be parsed.
     * @throws MMTPValidationException        If the application message is invalid according to the MMTP specification.
     */
    @Async("WorkerPool")
    protected void processSubjectCastApplicationMessage(ByteBuffer buffer) throws InvalidProtocolBufferException, MMTPValidationException
    {
        log.info("Processing subject cast application message");
        SubjectCastApplicationMessage applicationMessage = SubjectCastApplicationMessage.parseFrom(buffer);
        MMTPValidator.validate(applicationMessage);

        LocalSubjectMessageForwardRequest forwardRequest = new LocalSubjectMessageForwardRequest(this, applicationMessage);
        eventPublisher.publishEvent(forwardRequest);
    }
}