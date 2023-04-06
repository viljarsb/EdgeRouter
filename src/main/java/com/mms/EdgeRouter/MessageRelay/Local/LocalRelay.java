package com.mms.EdgeRouter.MessageRelay.Local;

import MMTPMessageFormats.DirectApplicationMessage;
import MMTPMessageFormats.MessageType;
import MMTPMessageFormats.ProtocolMessage;
import MMTPMessageFormats.SubjectCastApplicationMessage;
import com.google.protobuf.ByteString;
import com.mms.EdgeRouter.ConnectionManagement.IConnectionRepository;
import com.mms.EdgeRouter.MessageRelay.Local.MessageTracker.IMessageTracker;
import com.mms.EdgeRouter.MessageRelay.Events.LocalDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.LocalSubjectMessageForwardRequest;
import com.mms.EdgeRouter.SubscriptionManagement.ISubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A service that handles the forwarding of messages locally.
 * Implements {@link ILocalRelay} interface.
 */
@Service
@Slf4j
public class LocalRelay implements ILocalRelay
{
    private final IConnectionRepository connectionRepository;
    private final ISubscriptionRepository subscriptionRepository;
    private final IMessageTracker messageTracker;


    /**
     * Constructs a new {@link LocalRelay}.
     *
     * @param connectionRepository   The connection repository.
     * @param subscriptionRepository The subscription repository.
     */
    @Autowired
    public LocalRelay(IConnectionRepository connectionRepository, ISubscriptionRepository subscriptionRepository, IMessageTracker messageTracker)
    {
        this.connectionRepository = connectionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.messageTracker = messageTracker;
    }


    /**
     * Handles a {@link LocalDirectMessageForwardRequest} event.
     *
     * @param event The event.
     */
    @Async("WorkerPool")
    @EventListener
    @Override
    public void onLocalForwardingRequest(LocalDirectMessageForwardRequest event)
    {
        DirectApplicationMessage message = event.getMessage();
        log.info("Processing subject cast application message={}", message.getId());
        processDirectApplicationMessage(message);
    }


    /**
     * Handles a {@link LocalSubjectMessageForwardRequest} event.
     *
     * @param event The event.
     */
    @Async("WorkerPool")
    @EventListener
    @Override
    public void onLocalForwardingRequest(LocalSubjectMessageForwardRequest event)
    {
        SubjectCastApplicationMessage message = event.getMessage();
        log.info("Processing subject cast application message={}", message.getId());
        processSubjectCastApplicationMessage(message);
    }


    /**
     * Sends a direct application message to all subscribers that are present in the recipients list.
     *
     * @param message The message to send.
     */
    @Async("WorkerPool")
    protected void processDirectApplicationMessage(DirectApplicationMessage message)
    {
        List<String> recipients = message.getRecipientsList();
        List<String> agents = subscriptionRepository.getSubscribersByMrns(recipients);
        List<WebSocketSession> sessions = connectionRepository.getSessions(agents);
        sessions.removeIf(agent -> messageTracker.checkRebound(message.getId(), agent.getId()) || messageTracker.checkDeliveryStatus(message.getId(), agent.getId()));
        serializeAndSend(message.toByteString(), MessageType.DIRECT_APPLICATION_MESSAGE, sessions, message.getId());
    }


    /**
     * Sends a subject cast application message to all subscribers that are subscribed to the subject.
     *
     * @param message The message to send.
     */
    @Async("WorkerPool")
    protected void processSubjectCastApplicationMessage(SubjectCastApplicationMessage message)
    {
        String subject = message.getSubject();
        List<String> agents = subscriptionRepository.getSubscribersBySubject(subject);
        List<WebSocketSession> sessions = connectionRepository.getSessions(agents);
        sessions.removeIf(agent -> messageTracker.checkRebound(message.getId(), agent.getId()) || messageTracker.checkDeliveryStatus(message.getId(), agent.getId()));
        serializeAndSend(message.toByteString(), MessageType.SUBJECT_CAST_APPLICATION_MESSAGE, sessions, message.getId());
    }


    /**
     * Serializes a message to a {@link ByteBuffer}.
     *
     * @param message     The message to serialize.
     * @param messageType The type of the message.
     * @return The serialized message.
     */
    protected ByteBuffer serializeMessage(ByteString message, MessageType messageType)
    {
        ProtocolMessage protocolMessage = ProtocolMessage.newBuilder().setType(messageType).setContent(message).build();
        return ByteBuffer.wrap(protocolMessage.toByteArray());
    }


    /**
     * Sends a {@link ByteBuffer} to a list of {@link WebSocketSession}s.
     *
     * @param buffer   The buffer to send.
     * @param sessions The sessions to send the buffer to.
     */
    @Async("WorkerPool")
    protected void send(ByteBuffer buffer, List<WebSocketSession> sessions, String messageId)
    {
        for (WebSocketSession session : sessions)
        {
            if (session.isOpen())
            {
                try
                {
                    sendSocket(new BinaryMessage(buffer), session);
                    messageTracker.registerDelivery(session.getId(), messageId);
                }

                catch (IOException e)
                {
                    log.error("Error sending message={} to agent={}", messageId, session.getId(), e);
                }
            }
        }
    }


    /**
     * Sends a {@link BinaryMessage} to a {@link WebSocketSession}.
     *
     * @param message The message to send.
     * @param socket  The socket to send the message to.
     */
    @Async("WorkerPool")
    protected void sendSocket(BinaryMessage message, WebSocketSession socket) throws IOException
    {
        socket.sendMessage(message);
    }


    /**
     * Serializes a protocol message and sends it to a list of sessions.
     *
     * @param message     The message to send.
     * @param messageType The type of the message.
     * @param sessions    The sessions to send the message to.
     */
    @Async("WorkerPool")
    protected void serializeAndSend(ByteString message, MessageType messageType, List<WebSocketSession> sessions, String messageId)
    {
        ByteBuffer buffer = serializeMessage(message, messageType);
        send(buffer, sessions, messageId);
    }
}
