package com.mms.EdgeRouter.MessageRelay.Local.MessageTracker;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
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
@Repository
public class MessageTracker implements IMessageTracker
{

    private final Cache<String, String> reboundTracker;
    private final Cache<String, String> deliveryTracker;

    @Value("${mms.reboundTracker.maxSize:20000}")
    private int reboundTrackerMaxSize;

    @Value("${mms.reboundTracker.expireAfterWrite:10}")
    private int reboundTrackerExpiry;

    @Value("${mms.deliveryTracker.maxSize:20000}")
    private int deliveryTrackerMaxSize;

    @Value("${mms.deliveryTracker.expireAfterWrite:10}")
    private int deliveryTrackerExpiry;


    /**
     * Constructs a new {@link MessageTracker} with a cache that can hold 1000 messages for 10 minutes.
     */
    public MessageTracker()
    {
        this.reboundTracker = CacheBuilder.newBuilder()
                .maximumSize(reboundTrackerMaxSize)
                .expireAfterWrite(reboundTrackerExpiry, TimeUnit.MINUTES)
                .build();

        this.deliveryTracker = CacheBuilder.newBuilder()
                .maximumSize(deliveryTrackerMaxSize)
                .expireAfterWrite(deliveryTrackerExpiry, TimeUnit.MINUTES)
                .build();
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
        reboundTracker.put(agentID, messageId);
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
        String cachedMessageId = reboundTracker.getIfPresent(agentID);
        return cachedMessageId != null && cachedMessageId.equals(messageId);
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
        deliveryTracker.put(agentID, messageId);
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
        String cachedMessageId = deliveryTracker.getIfPresent(agentID);
        return cachedMessageId != null && cachedMessageId.equals(messageId);
    }
}
