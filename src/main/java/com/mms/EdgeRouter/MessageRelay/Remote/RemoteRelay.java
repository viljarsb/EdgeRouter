package com.mms.EdgeRouter.MessageRelay.Remote;

import Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import Protocols.MMTP.MessageFormats.MessageType;
import Protocols.MMTP.MessageFormats.ProtocolMessage;
import Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import com.google.protobuf.ByteString;
import com.mms.EdgeRouter.MessageRelay.Events.RemoteForwardRequestDirected;
import com.mms.EdgeRouter.MessageRelay.Events.RemoteForwardRequestSubjectCast;
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
 * This class represents a message relay that sends application messages to remote agents via JMS.
 */
@Slf4j
@Component
public class RemoteRelay
{
    private final JmsTemplate jmsTemplate;


    /**
     * Constructs a new instance of RemoteRelay.
     *
     * @param jmsTemplate The JmsTemplate to use for sending messages.
     */
    @Autowired
    public RemoteRelay(JmsTemplate jmsTemplate)
    {
        this.jmsTemplate = jmsTemplate;
    }


    /**
     * Processes a directed remote forwarding request.
     *
     * @param remoteForwardRequestDirected The RemoteForwardRequestDirected to process.
     */
    @Async("WorkerPool")
    @EventListener
    public void onRemoteForwardRequestDirected(RemoteForwardRequestDirected remoteForwardRequestDirected)
    {
        DirectApplicationMessage message = remoteForwardRequestDirected.getMessage();
        log.info("Processing direct application message with ID: {}", message.getId());
        List<String> destinations = remoteForwardRequestDirected.getRecipients();
        ByteBuffer buffer = serializeMessage(message.toByteString(), MessageType.DIRECT_APPLICATION_MESSAGE);
        for (String destination : destinations)
        {
            sendBytes(destination, buffer);
        }
    }


    /**
     * Processes a subject-cast remote forwarding request.
     *
     * @param remoteForwardRequestSubjectCast The RemoteForwardRequestSubjectCast to process.
     */
    @Async("WorkerPool")
    @EventListener
    public void onRemoteForwardRequestSubjectCast(RemoteForwardRequestSubjectCast remoteForwardRequestSubjectCast)
    {
        SubjectCastApplicationMessage message = remoteForwardRequestSubjectCast.getMessage();
        log.info("Processing subject cast application message with ID: {}", message.getId());
        String subject = remoteForwardRequestSubjectCast.getSubject();
        ByteBuffer buffer = serializeMessage(message.toByteString(), MessageType.SUBJECT_CAST_APPLICATION_MESSAGE);
        sendBytes(subject, buffer);
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
        return ProtocolMessage.newBuilder()
                .setType(messageType)
                .setContent(message)
                .build()
                .toByteString()
                .asReadOnlyByteBuffer();
    }


    /**
     * Sends a ByteBuffer to a JMS destination.
     *
     * @param destination The name of the JMS destination to send the message to.
     * @param payload     The ByteBuffer payload to send.
     */
    @Async("WorkerPool")
    public void sendBytes(String destination, ByteBuffer payload)
    {
        jmsTemplate.send(destination, session ->
        {
            BytesMessage message = session.createBytesMessage();
            message.writeBytes(payload.array());
            message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
            return message;
        });
    }
}