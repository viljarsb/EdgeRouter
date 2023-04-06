package com.mms.EdgeRouter.ActiveMQ;

import com.mms.EdgeRouter.ActiveMQ.events.RemoteMessageEvent;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

/**
 * RemoteReceiver is a class that implements the JMS MessageListener interface to receive messages from an ActiveMQ.
 * This class reads a Message object from the ActiveMQ broker and converts it to a ByteBuffer.
 * It then publishes a RemoteMessageEvent using the Spring ApplicationEventPublisher.
 */
@Slf4j
@Service
public class RemoteReceiver implements MessageListener
{
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Constructs a new {@link RemoteReceiver} with the given ApplicationEventPublisher.
     *
     * @param eventPublisher The ApplicationEventPublisher used to publish RemoteMessageEvents.
     */
    @Autowired
    public RemoteReceiver(ApplicationEventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }


    /**
     * This method is called when a new message is received from the ActiveMQ broker.
     * It reads the message payload into a byte array and converts it to a ByteBuffer.
     * It then publishes a {@link RemoteMessageEvent} using the Spring ApplicationEventPublisher.
     *
     * @param message The Message object received from the ActiveMQ broker.
     */
    @Override
    public void onMessage(Message message)
    {
        try
        {
            log.info("Received message from topic {}: {}", message.getJMSDestination(), message);
        }
        catch (JMSException ex)
        {
            log.error("Error getting message destination", ex);
        }

        try
        {
            if (message instanceof BytesMessage bytesMessage)
            {
                byte[] data = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(data);
                ByteBuffer buffer = ByteBuffer.wrap(data);
                log.info("Received message from topic {}: {}", message.getJMSDestination(), buffer);
                RemoteMessageEvent remoteMessageEvent = new RemoteMessageEvent(this, buffer);
                eventPublisher.publishEvent(remoteMessageEvent);
            }
            else
            {
                log.warn("Received unsupported message type: {}", message.getClass().getName());
            }
        }
        catch (JMSException ex)
        {
            log.error("Error processing received message", ex);
        }
    }
}
