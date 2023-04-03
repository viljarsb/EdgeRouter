package com.mms.EdgeRouter.MessageRelay.Local;

import Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import Protocols.MMTP.MessageFormats.MessageType;
import Protocols.MMTP.MessageFormats.ProtocolMessage;
import Protocols.MMTP.MessageFormats.SubjectCastApplicationMessage;
import com.google.protobuf.ByteString;
import com.mms.EdgeRouter.ConnectionManagement.IConnectionRepository;
import com.mms.EdgeRouter.MessageRelay.Events.LocalDirectForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.LocalSubjectForwardRequest;
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


@Service
@Slf4j
public class LocalRelay implements ILocalRelay
{
    private final IConnectionRepository connectionRepository;
    private final ISubscriptionRepository subscriptionRepository;


    @Autowired
    public LocalRelay(IConnectionRepository connectionRepository, ISubscriptionRepository subscriptionRepository)
    {
        this.connectionRepository = connectionRepository;
        this.subscriptionRepository = subscriptionRepository;
    }


    @Async("WorkerPool")
    @EventListener
    @Override
    public void onLocalForwardingRequest(LocalDirectForwardRequest event)
    {
        DirectApplicationMessage message = event.getMessage();
        log.info("Processing subject cast application message with ID: {}", message.getId());
        processDirectApplicationMessage(message);
    }


    @Async("WorkerPool")
    @EventListener
    @Override
    public void onLocalForwardingRequest(LocalSubjectForwardRequest event)
    {
        SubjectCastApplicationMessage message = event.getMessage();
        log.info("Processing subject cast application message with ID: {}", message.getId());
        processSubjectCastApplicationMessage(message);
    }


    @Async("WorkerPool")
    protected void processDirectApplicationMessage(DirectApplicationMessage message)
    {
        List<String> recipients = message.getRecipientsList();
        List<String> agents = subscriptionRepository.getSubscribersByMrns(recipients);
        List<WebSocketSession> sessions = connectionRepository.getSessions(agents);
        ByteBuffer buffer = serializeMessage(message.toByteString(), MessageType.DIRECT_APPLICATION_MESSAGE);
        send(buffer, sessions);
    }


    @Async("WorkerPool")
    protected void processSubjectCastApplicationMessage(SubjectCastApplicationMessage message)
    {
        String subject = message.getSubject();
        List<String> agents = subscriptionRepository.getSubscribersBySubject(subject);
        List<WebSocketSession> sessions = connectionRepository.getSessions(agents);
        ByteBuffer buffer = serializeMessage(message.toByteString(), MessageType.SUBJECT_CAST_APPLICATION_MESSAGE);
        send(buffer, sessions);
    }


    protected ByteBuffer serializeMessage(ByteString message, MessageType messageType)
    {
        return ProtocolMessage.newBuilder()
                .setType(messageType)
                .setContent(message)
                .build()
                .toByteString()
                .asReadOnlyByteBuffer();
    }


    @Async("WorkerPool")
    protected void send(ByteBuffer buffer, List<WebSocketSession> sessions)
    {
        for (WebSocketSession session : sessions)
        {
            if (session.isOpen())
            {
                sendSocket(new BinaryMessage(buffer), session);
            }
        }
    }


    @Async("WorkerPool")
    protected void sendSocket(BinaryMessage message, WebSocketSession socket)
    {
        try
        {
            if (socket.isOpen())
            {
                socket.sendMessage(message);
            }
        }

        catch (IOException ex)
        {
            log.error("Failed to send message to session: {}", socket.getId());
        }
    }
}
