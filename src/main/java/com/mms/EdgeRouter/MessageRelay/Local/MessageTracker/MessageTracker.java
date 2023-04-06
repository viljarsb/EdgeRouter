package com.mms.EdgeRouter.MessageRelay.Local.MessageTracker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;


/**
 * A simple cache to keep track of messages that have been sent, so we don't send them back to the same agent, and
 * to track which messages have been delivered to which agents.
 * <p>
 * Does not store indefinitely, because messages can have up to 30 days of TTL, which could be a lot.
 * <p>
 * A better solution should be implemented in v2.
 */
@Slf4j
public class MessageTracker implements IMessageTracker
{
    private final Cache<String, String> reboundTracker;
    private final Cache<String, String> deliveryTracker;


    /**
     * Constructs a new {@link MessageTracker} with the given caches.
     */
    public MessageTracker(@Value("${mms.reboundTracker.maxSize:20000}") int reboundTrackerMaxSize, @Value("${mms.reboundTracker.expireAfterWrite:10}") int reboundTrackerExpiry, @Value("${mms.deliveryTracker.maxSize:20000}") int deliveryTrackerMaxSize, @Value("${mms.deliveryTracker.expireAfterWrite:10}") int deliveryTrackerExpiry)
    {
        this.reboundTracker = CacheBuilder.newBuilder()
                .maximumSize(reboundTrackerMaxSize)
                .expireAfterWrite(reboundTrackerExpiry, TimeUnit.MINUTES)
                .build();

        this.deliveryTracker = CacheBuilder.newBuilder()
                .maximumSize(deliveryTrackerMaxSize)
                .expireAfterWrite(deliveryTrackerExpiry, TimeUnit.MINUTES)
                .build();

        log.info("MessageTracker initialized with reboundTrackerMaxSize={}, reboundTrackerExpiry={}, deliveryTrackerMaxSize={}, " +
                "deliveryTrackerExpiry={}", reboundTrackerMaxSize, reboundTrackerExpiry, deliveryTrackerMaxSize, deliveryTrackerExpiry);
    }


    /**
     * Adds a message to the rebound tracker cache.
     *
     * @param messageId The ID of the message.
     * @param agentID   The ID of the agent that sent the message.
     */
    @Override
    public void registerSent(String messageId, String agentID)
    {
        log.info("Registering sent message={} from agent={}", messageId, agentID);
        reboundTracker.put(messageId, agentID);
    }


    /**
     * Checks if the agent sent this message.
     *
     * @param messageId The ID of the message.
     * @param agentID   The ID of the agent.
     * @return True if the agent sent this message, false otherwise.
     */
    @Override
    public boolean checkRebound(String messageId, String agentID)
    {
        String cachedAgentId = reboundTracker.getIfPresent(messageId);
        return cachedAgentId != null && cachedAgentId.equals(agentID);
    }


    /**
     * Registers delivery of a message to an agent.
     *
     * @param messageId The ID of the message.
     * @param agentID   The ID of the agent that delivered the message.
     */
    @Override
    public void registerDelivery(String messageId, String agentID)
    {
        log.info("Registering delivery of message={} to agent={}", messageId, agentID);
        deliveryTracker.put(messageId, agentID);
    }


    /**
     * Checks if a message has been delivered to an agent.
     *
     * @param messageId The ID of the message.
     * @param agentID   The ID of the agent.
     * @return True if the message has been delivered by the agent, false otherwise.
     */
    @Override
    public boolean checkDeliveryStatus(String messageId, String agentID)
    {
        String cachedAgentId = deliveryTracker.getIfPresent(messageId);
        return cachedAgentId != null && cachedAgentId.equals(agentID);
    }
}
