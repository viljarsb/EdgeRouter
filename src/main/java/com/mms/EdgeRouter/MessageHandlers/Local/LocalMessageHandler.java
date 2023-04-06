package com.mms.EdgeRouter.MessageHandlers.Local;

import Protocols.MMTP.Exceptions.MMTPValidationException;
import Protocols.MMTP.MessageFormats.*;
import Protocols.MMTP.Validators.MMTPValidator;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionCloseRequest;
import com.mms.EdgeRouter.ConnectionManagement.IConnectionRepository;
import com.mms.EdgeRouter.MessageHandlers.Events.*;
import com.mms.EdgeRouter.MessageRelay.Events.LocalDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.LocalSubjectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.RemoteDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.RemoteSubjectMessageForwardRequest;
import com.mms.EdgeRouter.SubscriptionManagement.Events.SubscriptionEventType;
import com.mms.EdgeRouter.WebSocket.Events.LocalMessageEvent;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for handling local messages received from agents.
 * Parses the messages and delegates to the appropriate message processor.
 * Publishes events to the event bus so that other services can handle the messages.
 * Implements {@link ILocalMessageHandler} interface to handle {@link LocalMessageEvent}s.
 */
@Service
@Slf4j
public class LocalMessageHandler implements ILocalMessageHandler
{
    private final IConnectionRepository connectionRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Constructs a new {@link LocalMessageHandler} with the given dependencies.
     *
     * @param connectionRepository The connection repository to use.
     * @param eventPublisher       The event publisher to use.
     */
    @Autowired
    public LocalMessageHandler(IConnectionRepository connectionRepository, ApplicationEventPublisher eventPublisher)
    {
        this.connectionRepository = connectionRepository;
        this.eventPublisher = eventPublisher;
    }


    /**
     * Handles a LocalMessageEvent asynchronously.
     *
     * @param event The LocalMessageEvent to handle.
     */
    @Async("WorkerPool")
    @EventListener
    @Override
    public void onLocalMessage(LocalMessageEvent event)
    {
        String agentID = event.getAgentID();
        ByteBuffer buffer = event.getBuffer();
        handleLocalMessage(buffer, agentID);
    }


    /**
     * Handles a local message by parsing it and delegating to the appropriate message processor.
     *
     * @param buffer  The buffer containing the message.
     * @param agentID The ID of the agent that sent the message.
     */
    @Async("WorkerPool")
    protected void handleLocalMessage(ByteBuffer buffer, String agentID)
    {
        try
        {
            ProtocolMessage message = ProtocolMessage.parseFrom(buffer);
            MessageType type = message.getType();
            ByteBuffer payload = ByteBuffer.wrap(message.getContent().toByteArray());

            switch (type)
            {
                case DIRECT_APPLICATION_MESSAGE, SUBJECT_CAST_APPLICATION_MESSAGE -> processApplicationMessage(payload, type, agentID);
                case REGISTER, UNREGISTER -> processRegistrationMessage(payload, type, agentID);
            }
        }

        catch (InvalidProtocolBufferException ex)
        {
            log.warn("Error parsing local message", ex);
            sendCloseRequest(agentID, WebSocketCloseStatus.PROTOCOL_ERROR.code(), "Client sent invalid message");
        }

        catch (MMTPValidationException ex)
        {
            log.warn("Error validating local message", ex);
            sendCloseRequest(agentID, WebSocketCloseStatus.PROTOCOL_ERROR.code(), "Client sent invalid message");
        }
    }


    /**
     * Processes an application message by delegating to the appropriate processor based on its type.
     *
     * @param buffer  The buffer containing the application message.
     * @param type    The type of the application message.
     * @param agentID The ID of the agent that sent the message.
     * @throws InvalidProtocolBufferException If the application message could not be parsed.
     * @throws MMTPValidationException        If the application message is invalid according to the MMTP specification.
     */
    @Async("WorkerPool")
    protected void processApplicationMessage(ByteBuffer buffer, MessageType type, String agentID) throws InvalidProtocolBufferException, MMTPValidationException
    {
        Optional<String> MRN = connectionRepository.getMRN(agentID);

        if (MRN.isEmpty())
        {
            log.warn("Received application message from unauthenticated agent {}, requesting closure of connection.", agentID);
            sendCloseRequest(agentID, WebSocketCloseStatus.POLICY_VIOLATION.code(), "Client sent application message before authentication");
            return;
        }

        switch (type)
        {
            case DIRECT_APPLICATION_MESSAGE -> processDirectApplicationMessage(buffer, agentID);
            case SUBJECT_CAST_APPLICATION_MESSAGE -> processSubjectCastApplicationMessage(buffer, agentID);
        }
    }


    /**
     * Processes a direct application message by validating it, creating a local and a remote forward request, and publishing them as events.
     *
     * @param buffer  The buffer containing the direct application message.
     * @param agentID The ID of the agent sending the message.
     * @throws MMTPValidationException        If the message fails MMTP validation.
     * @throws InvalidProtocolBufferException If the message is invalid.
     */
    @Async("WorkerPool")
    protected void processDirectApplicationMessage(ByteBuffer buffer, String agentID) throws InvalidProtocolBufferException, MMTPValidationException
    {
        log.debug("Received direct application message from agent {}", agentID);

        DirectApplicationMessage applicationMessage = DirectApplicationMessage.parseFrom(buffer);
        MMTPValidator.validate(applicationMessage);

        LocalDirectMessageForwardRequest localForwardRequest = new LocalDirectMessageForwardRequest(this, applicationMessage);
        RemoteDirectMessageForwardRequest remoteForwardingRequest = new RemoteDirectMessageForwardRequest(this, applicationMessage);

        eventPublisher.publishEvent(localForwardRequest);
        eventPublisher.publishEvent(remoteForwardingRequest);
    }


    /**
     * Processes a subject cast application message by validating it, creating a local and a remote forward request, and publishing them as events.
     *
     * @param buffer  The buffer containing the subject cast application message.
     * @param agentID The ID of the agent sending the message.
     * @throws MMTPValidationException        If the message fails MMTP validation.
     * @throws InvalidProtocolBufferException If the message is invalid.
     */
    @Async("WorkerPool")
    protected void processSubjectCastApplicationMessage(ByteBuffer buffer, String agentID) throws MMTPValidationException, InvalidProtocolBufferException
    {
        log.debug("Received subject cast application message from agent {}", agentID);

        SubjectCastApplicationMessage applicationMessage = SubjectCastApplicationMessage.parseFrom(buffer);
        MMTPValidator.validate(applicationMessage);

        LocalSubjectMessageForwardRequest localForwardRequest = new LocalSubjectMessageForwardRequest(this, applicationMessage);
        RemoteSubjectMessageForwardRequest remoteForwardingRequest = new RemoteSubjectMessageForwardRequest(this, applicationMessage);

        eventPublisher.publishEvent(localForwardRequest);
        eventPublisher.publishEvent(remoteForwardingRequest);
    }


    /**
     * Processes a registration message by delegating to the appropriate processor based on its type.
     *
     * @param buffer  The buffer containing the registration message.
     * @param type    The type of the registration message.
     * @param agentID The ID of the agent that sent the message.
     * @throws MMTPValidationException        if the message fails validation.
     * @throws InvalidProtocolBufferException if the message is not a valid protocol buffer.
     */
    @Async("WorkerPool")
    protected void processRegistrationMessage(ByteBuffer buffer, MessageType type, String agentID) throws MMTPValidationException, InvalidProtocolBufferException
    {
        switch (type)
        {
            case REGISTER -> processRegisterMessage(buffer, agentID);
            case UNREGISTER -> processUnregisterMessage(buffer, agentID);
        }
    }


    /**
     * Processes a REGISTER message.
     *
     * @param buffer  The buffer containing the REGISTER message.
     * @param agentID The ID of the agent that sent the message.
     * @throws MMTPValidationException        if the message fails validation.
     * @throws InvalidProtocolBufferException if the message is not a valid protocol buffer.
     */
    @Async("WorkerPool")
    protected void processRegisterMessage(ByteBuffer buffer, String agentID) throws InvalidProtocolBufferException, MMTPValidationException
    {
        log.debug("Received registration message from agent {}", agentID);

        Register register = Register.parseFrom(buffer);
        MMTPValidator.validate(register);

        List<String> subjects = register.getInterestsList();

        if (!subjects.isEmpty())
        {
            SubjectSubscriptionRequestEvent subscriptionSubjectRequest = new SubjectSubscriptionRequestEvent(this, agentID, subjects, SubscriptionEventType.SUBSCRIPTION);
            eventPublisher.publishEvent(subscriptionSubjectRequest);
        }

        if (register.hasWantDirectMessages())
        {
            MrnSubscriptionRequestEvent subscriptionMrnRequest = new MrnSubscriptionRequestEvent(this, agentID, SubscriptionEventType.SUBSCRIPTION);
            eventPublisher.publishEvent(subscriptionMrnRequest);
        }
    }


    /**
     * Processes an UNREGISTER message.
     *
     * @param buffer  The buffer containing the UNREGISTER message.
     * @param agentID The ID of the agent that sent the message.
     * @throws MMTPValidationException        if the message fails validation.
     * @throws InvalidProtocolBufferException if the message is not a valid protocol buffer.
     */
    @Async("WorkerPool")
    protected void processUnregisterMessage(ByteBuffer buffer, String agentID) throws InvalidProtocolBufferException, MMTPValidationException
    {
        log.debug("Received unregistration message from agent {}", agentID);

        Register register = Register.parseFrom(buffer);
        MMTPValidator.validate(register);

        List<String> subjects = register.getInterestsList();

        if (!subjects.isEmpty())
        {
            SubjectSubscriptionRequestEvent subscriptionSubjectRequest = new SubjectSubscriptionRequestEvent(this, agentID, subjects, SubscriptionEventType.UNSUBSCRIPTION);
            eventPublisher.publishEvent(subscriptionSubjectRequest);
        }

        if (register.hasWantDirectMessages())
        {
            MrnSubscriptionRequestEvent subscriptionMrnRequest = new MrnSubscriptionRequestEvent(this, agentID, SubscriptionEventType.UNSUBSCRIPTION);
            eventPublisher.publishEvent(subscriptionMrnRequest);
        }
    }


    /**
     * Sends a WebSocket close request to the client with the specified status code and reason.
     *
     * @param agentID    The ID of the agent to close the WebSocket connection for.
     * @param statusCode The status code to include in the close request.
     * @param reason     The reason to include in the close request.
     */
    @Async("WorkerPool")
    protected void sendCloseRequest(String agentID, int statusCode, String reason)
    {
        ConnectionCloseRequest request = new ConnectionCloseRequest(this, agentID, statusCode, reason);
        eventPublisher.publishEvent(request);
    }
}
