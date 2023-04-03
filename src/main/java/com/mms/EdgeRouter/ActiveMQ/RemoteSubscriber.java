package com.mms.EdgeRouter.ActiveMQ;


import jakarta.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class RemoteSubscriber
{

    private final ConnectionFactory connectionFactory;
    private final RemoteReceiver activeMQReceiver;

    private final ConcurrentHashMap<String, DefaultMessageListenerContainer> listenerContainers = new ConcurrentHashMap<>();


    @Autowired
    public RemoteSubscriber(@Qualifier("activeMQConnectionFactory") ConnectionFactory connectionFactory, RemoteReceiver activeMQReceiver)
    {
        this.connectionFactory = connectionFactory;
        this.activeMQReceiver = activeMQReceiver;
    }


    /**
     * Subscribes to the specified topic if not already subscribed.
     * Creates a new DefaultMessageListenerContainer and starts listening for messages.
     *
     * @param topicName The name of the topic to subscribe to.
     */
    public void subscribe(String topicName)
    {
        if (!listenerContainers.containsKey(topicName))
        {
            DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setDestinationName(topicName);

            container.setMessageListener(activeMQReceiver);
            container.setSubscriptionDurable(false);
            container.setPubSubDomain(true); // Set to true for topics, false for queues
            container.afterPropertiesSet(); // Initialize the container
            container.start(); // Start listening

            listenerContainers.put(topicName, container);
            log.info("Subscribed to topic: {}", topicName);
        }
        else
        {
            log.warn("Already subscribed to topic: {}", topicName);
        }
    }


    /**
     * Unsubscribes from the specified topic if currently subscribed.
     * Stops listening for messages and cleans up resources.
     *
     * @param topicName The name of the topic to unsubscribe from.
     */
    public void unsubscribe(String topicName)
    {
        DefaultMessageListenerContainer container = listenerContainers.remove(topicName);
        if (container != null)
        {
            container.stop(); // Stop listening
            container.destroy(); // Clean up resources

            log.info("Unsubscribed from topic: {}", topicName);
        }
        else
        {
            log.warn("Not subscribed to topic: {}", topicName);
        }
    }
}