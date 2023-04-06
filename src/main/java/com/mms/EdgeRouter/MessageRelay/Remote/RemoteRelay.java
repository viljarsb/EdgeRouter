package com.mms.EdgeRouter.MessageRelay.Remote;

import Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import Protocols.MMTP.MessageFormats.MessageType;
import Protocols.MMTP.MessageFormats.ProtocolMessage;
import Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import com.google.protobuf.ByteString;
import com.mms.EdgeRouter.MessageRelay.Events.LocalDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.RemoteDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.RemoteSubjectMessageForwardRequest;
import io.netty.buffer.ByteBuf;
import jakarta.jms.BytesMessage;
import jakarta.jms.DeliveryMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * This class represents a message relay that sends application messages to remote agents via JMS/ActiveMQ over TCP.
 */
@Slf4j
@Component
public class RemoteRelay
{
    private final JmsTemplate jmsTemplate;


    /**
     * Constructs a new instance of {@link RemoteRelay}.
     *
     * @param jmsTemplate The JmsTemplate to use for sending messages.
     */
    @Autowired
    public RemoteRelay(JmsTemplate jmsTemplate)
    {
        this.jmsTemplate = jmsTemplate;
    }


    /**
     * Handles a {@link RemoteDirectMessageForwardRequest} event.
     *
     * @param event The event.
     */
    @Async("WorkerPool")
    @EventListener
    public void onRemoteForwardRequestDirected(RemoteDirectMessageForwardRequest event)
    {
        DirectApplicationMessage message = event.getMessage();
        log.info("Processing direct application message with ID: {}", message.getId());
        List<String> destinations = event.getRecipients();
        serializeAndSend(MessageType.DIRECT_APPLICATION_MESSAGE, message.toByteString(), destinations);
    }


    /**
     * Handles a {@link RemoteSubjectMessageForwardRequest} event.
     *
     * @param event The event.
     */
    @Async("WorkerPool")
    @EventListener
    public void onRemoteForwardRequestSubjectCast(RemoteSubjectMessageForwardRequest event)
    {
        SubjectCastApplicationMessage message = event.getMessage();
        log.info("Processing subject cast application message with ID: {}", message.getId());
        String subject = event.getSubject();
        serializeAndSend(MessageType.SUBJECT_CAST_APPLICATION_MESSAGE, message.toByteString(), List.of(subject));
    }


    /**
     * Serializes an application message into a ByteBuffer.
     *
     * @param message     The ByteString message to serialize.
     * @param messageType The MessageType to use for the serialized message.
     * @return A ByteBuffer containing the serialized message.
     */
    protected ByteBuffer serializeMessage(ByteString message, MessageType messageType)
    {
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder()
                .setType(messageType)
                .setContent(message)
                .build();

        return ByteBuffer.wrap(protocolMessage.toByteArray());
    }


    /**
     * Sends a ByteBuffer to a JMS destination.
     *
     * @param destination The name of the JMS destination to send the message to.
     * @param payload     The ByteBuffer payload to send.
     */
    @Async("WorkerPool")
    protected void sendBytes(String destination, ByteBuffer payload)
    {
        jmsTemplate.send(destination, session ->
        {
            BytesMessage message = session.createBytesMessage();
            message.writeBytes(payload.array());
            message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
            return message;
        });
    }


    /**
     * Serializes and sends a message to a list of destinations.
     *
     * @param messageType  The MessageType to use for the serialized message.
     * @param message      The ByteString message to serialize.
     * @param destinations The list of destinations to send the message to.
     */
    @Async("WorkerPool")
    protected void serializeAndSend(MessageType messageType, ByteString message, List<String> destinations)
    {
        ByteBuffer buffer = serializeMessage(message, messageType);

        for (String destination : destinations)
        {
            sendBytes(destination, buffer);
        }
    }
}