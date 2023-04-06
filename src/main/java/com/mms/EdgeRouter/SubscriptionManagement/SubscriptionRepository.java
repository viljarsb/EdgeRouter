package com.mms.EdgeRouter.SubscriptionManagement;

import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionAddedEvent;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionCloseRequest;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionRemovedEvent;
import com.mms.EdgeRouter.ConnectionManagement.IConnectionRepository;
import com.mms.EdgeRouter.MessageHandlers.Events.MrnSubscriptionRequestEvent;
import com.mms.EdgeRouter.MessageHandlers.Events.SubjectSubscriptionRequestEvent;
import com.mms.EdgeRouter.SubscriptionManagement.Events.MrnSubscriptionEvent;
import com.mms.EdgeRouter.SubscriptionManagement.Events.SubjectSubscriptionEvent;
import com.mms.EdgeRouter.SubscriptionManagement.Events.SubscriptionEventType;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * Repository for managing subscriptions to subjects and MRNs. Subscriptions can be added and removed
 * for individual subjects and MRNs, and clients can query for the current set of subscribers to a given
 * subject or MRN.
 * <p>
 * This class is event-driven, meaning that it listens for events and updates its state accordingly.
 * The following events are handled:
 * - ConnectionAddedEvent: Subscribes to the MRN associated with the connection, if available.
 * - ConnectionRemovedEvent: Unsubscribes from all subjects and the MRN associated with the connection.
 * - SubjectSubscriptionRequestEvent: Subscribes or unsubscribes the agent to the specified subjects.
 * - MrnSubscriptionRequestEvent: Subscribes or unsubscribes the agent to the MRN associated with their connection.
 * <p>
 * An event is published when the number of subscribers for a subject or an MRN goes from 0 to 1.
 * An event is also published when the number of subscribers for a subject or an MRN goes from 1 to 0.
 * No events are published for other subscriber count changes.
 * Implements {@link ISubscriptionRepository} interface.
 */
@Repository
@Slf4j
public class SubscriptionRepository implements ISubscriptionRepository
{
    private final Map<String, ConcurrentSkipListSet<String>> subscriptionsBySubject = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentSkipListSet<String>> subscriptionsByMrn = new ConcurrentHashMap<>();

    private final IConnectionRepository connectionRepository;
    private final ApplicationEventPublisher eventPublisher;


    /**
     * Constructor for {@link SubscriptionRepository}.
     *
     * @param connectionRepository The connection repository used to get PKIIdentity associated with agents.
     * @param eventPublisher       The event publisher used to publish subscription events.
     */
    @Autowired
    public SubscriptionRepository(@NonNull IConnectionRepository connectionRepository, @NonNull ApplicationEventPublisher eventPublisher)
    {
        this.connectionRepository = connectionRepository;
        this.eventPublisher = eventPublisher;
    }


    /**
     * Handles a {@link ConnectionAddedEvent}.
     * Subscribes to the MRN associated with the connection, if available.
     * An event is published when the number of subscribers for an MRN goes from 0 to 1.
     *
     * @param event The ConnectionAddedEvent to handle.
     */
    @Async("WorkerPool")
    @EventListener
    public void handleConnectionAdded(ConnectionAddedEvent event)
    {
        String agentId = event.getAgentID();
        Optional<String> mrnOptional = event.getMRN();
        onConnectionAdded(agentId, mrnOptional);
        log.debug("SubscriptionRepository: handleConnectionAdded: agent={} mrn={}", agentId, mrnOptional.orElse(null));
    }


    /**
     * Handles a {@link ConnectionRemovedEvent}.
     * Unsubscribes from all subjects and the MRN associated with the connection.
     * An event is published when the number of subscribers for a subject or an MRN goes from 1 to 0.
     *
     * @param event The ConnectionRemovedEvent to handle.
     */
    @Async("WorkerPool")
    @EventListener
    public void handleConnectionRemoved(ConnectionRemovedEvent event)
    {
        String agentId = event.getAgentID();
        Optional<String> mrnOptional = event.getMRN();
        onConnectionRemoved(agentId, mrnOptional);
        log.debug("SubscriptionRepository: handleConnectionRemoved: agent={}, mrn={}", agentId, mrnOptional.orElse(null));
    }


    /**
     * Handles a {@link SubjectSubscriptionRequestEvent}.
     * Subscribes or unsubscribes the agent to the specified subjects.
     *
     * @param event The SubjectSubscriptionRequestEvent to handle.
     */
    @Async("WorkerPool")
    @EventListener
    public void handleSubjectSubscribeRequest(SubjectSubscriptionRequestEvent event)
    {
        String agentId = event.getAgentID();
        List<String> subjects = event.getSubjects();

        if (event.getType() == SubscriptionEventType.SUBSCRIPTION)
        {
            subscribeToSubjects(subjects, agentId);
        }
        else if (event.getType() == SubscriptionEventType.UNSUBSCRIPTION)
        {
            unsubscribeFromSubjects(subjects, agentId);
        }

        log.debug("SubscriptionRepository: handleSubjectSubscribeRequest: agent={}, type={}, subjects={}", agentId, event.getType(), subjects);
    }


    /**
     * Handles a {@link MrnSubscriptionRequestEvent} event.
     * Subscribes or unsubscribes the agent to the MRN associated with their connection.
     *
     * @param event The MrnSubscriptionRequestEvent event to handle.
     */
    @Async("WorkerPool")
    @EventListener
    public void handleMrnSubscribeRequest(MrnSubscriptionRequestEvent event)
    {
        String agentId = event.getAgentID();

        if (event.getType() == SubscriptionEventType.SUBSCRIPTION)
        {
            subscribeToMrn(agentId);
        }
        else if (event.getType() == SubscriptionEventType.UNSUBSCRIPTION)
        {
            unsubscribeFromMrn(agentId);
        }

        log.debug("SubscriptionRepository: handleMrnSubscribeRequest: agent={} type={}", agentId, event.getType());
    }


    /**
     * Returns a list of agent IDs that are currently subscribed to the given subject.
     *
     * @param subject The subject to retrieve subscribers for.
     * @return A list of agent IDs that are currently subscribed to the given subject.
     */
    @Override
    public List<String> getSubscribersBySubject(String subject)
    {
        return new ArrayList<>(subscriptionsBySubject.getOrDefault(subject, new ConcurrentSkipListSet<>()));
    }


    /**
     * Returns a list of agent IDs that are currently subscribed to any of the given subjects.
     *
     * @param subjects The list of subjects to retrieve subscribers for.
     * @return A list of agent IDs that are currently subscribed to any of the given subjects.
     *                    If no agents are subscribed to any of the given subjects, an empty list is returned.
     */
    @Override
    public List<String> getSubscribersBySubjects(List<String> subjects)
    {
        List<String> subscribers = new ArrayList<>();
        subjects.forEach(subject -> subscribers.addAll(getSubscribersBySubject(subject)));
        return subscribers;
    }


    /**
     * Returns a map of subjects to a list of agent IDs that are currently subscribed to the given subject.
     *
     * @return A map of subjects to a list of agent IDs that are currently subscribed to the given subject.
     *                   If no agents are subscribed to any subjects, an empty map is returned.
     */
    @Override
    public List<String> getSubscribersByMrn(String mrn)
    {
        return new ArrayList<>(subscriptionsByMrn.getOrDefault(mrn, new ConcurrentSkipListSet<>()));
    }


    /**
     * Returns a list of agent IDs that are currently subscribed to the given MRN.
     *
     * @param mrns The MRN to retrieve subscribers for.
     * @return A list of agent IDs that are currently subscribed to the given MRN.
     *                   If no agents are subscribed to the given MRN, an empty list is returned.
     */
    @Override
    public List<String> getSubscribersByMrns(List<String> mrns)
    {
        List<String> subscribers = new ArrayList<>();
        mrns.forEach(mrn -> subscribers.addAll(getSubscribersByMrn(mrn)));
        return subscribers;
    }


    @Override
    public Map<String, List<String>> getSubjectSubscriptionMap()
    {
        Map<String, List<String>> map = new HashMap<>();
        subscriptionsBySubject.forEach((subject, set) -> map.put(subject, new ArrayList<>(set)));
        return map;
    }


    /**
     * Returns a map of MRNs to a list of agent IDs that are currently subscribed to any MRN.
     *
     * @return A map of MRNs to a list of agent IDs that are currently subscribed to the given MRN.
     *                  If no agents are subscribed to any MRNs, an empty map is returned.
     */
    @Override
    public Map<String, List<String>> getMrnSubscriptionMap()
    {
        Map<String, List<String>> map = new HashMap<>();
        subscriptionsByMrn.forEach((mrn, set) -> map.put(mrn, new ArrayList<>(set)));
        return map;
    }


    /**
     * This method is called when a connection is added.
     * If the connection has an associated MRN, the agent will be subscribed to that MRN.
     * An event is published when the number of subscribers for an MRN goes from 0 to 1.
     *
     * @param agentID     The agent ID associated with the connection.
     * @param mrnOptional The optional MRN associated with the agent.
     */
    @Async("WorkerPool")
    protected void onConnectionAdded(String agentID, Optional<String> mrnOptional)
    {
        if (mrnOptional.isPresent())
        {
            String mrn = mrnOptional.get();
            subscriptionsByMrn.computeIfAbsent(mrn, k ->
            {
                MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(this, mrn, SubscriptionEventType.SUBSCRIPTION);
                eventPublisher.publishEvent(mrnSubscriptionEvent);
                return new ConcurrentSkipListSet<>();
            }).add(agentID);
        }
    }


    /**
     * This method is called when a connection is removed.
     * The agent will be unsubscribed from all subjects and the associated MRN, if any.
     * An event is published when the number of subscribers for a subject or an MRN goes from 1 to 0.
     *
     * @param agentID     The agent ID associated with the connection.
     * @param mrnOptional The optional MRN associated with the agent.
     */
    @Async("WorkerPool")
    protected void onConnectionRemoved(String agentID, Optional<String> mrnOptional)
    {
        subscriptionsBySubject.forEach((key, set) ->
        {
            set.remove(agentID);
            if (set.isEmpty())
            {
                subscriptionsBySubject.remove(key);
                SubjectSubscriptionEvent subjectSubscriptionEvent = new SubjectSubscriptionEvent(this, key, SubscriptionEventType.UNSUBSCRIPTION);
                eventPublisher.publishEvent(subjectSubscriptionEvent);
            }
        });

        // Remove agentID from subscriptionsByMrn map
        if (mrnOptional.isPresent())
        {
            String mrn = mrnOptional.get();
            subscriptionsByMrn.computeIfPresent(mrn, (key, set) ->
            {
                set.remove(agentID);
                if (set.isEmpty())
                {
                    subscriptionsByMrn.remove(mrn);
                    // Publish event to notify subscribers of change
                    MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(this, mrn, SubscriptionEventType.UNSUBSCRIPTION);
                    eventPublisher.publishEvent(mrnSubscriptionEvent);
                }
                return set;
            });
        }
    }


    /**
     * Subscribes the agent to the given subjects.
     * An event is published when the number of subscribers for a subject goes from 0 to 1.
     *
     * @param subjects The list of subjects to subscribe to.
     * @param agentID  The agent ID to be subscribed.
     */
    @Async("WorkerPool")
    protected void subscribeToSubjects(List<String> subjects, String agentID)
    {
        subjects.forEach(s ->
        {
            subscriptionsBySubject.compute(s, (k, v) ->
            {
                if (v == null)
                {
                    v = new ConcurrentSkipListSet<>();
                    SubjectSubscriptionEvent subjectSubscriptionEvent = new SubjectSubscriptionEvent(this, s, SubscriptionEventType.SUBSCRIPTION);
                    eventPublisher.publishEvent(subjectSubscriptionEvent);
                }
                v.add(agentID);
                return v;
            });
        });
    }


    /**
     * Unsubscribes the agent from the given subjects.
     * An event is published when the number of subscribers for a subject goes from 1 to 0.
     *
     * @param subjects The list of subjects to unsubscribe from.
     * @param agentID  The agent ID to be unsubscribed.
     */
    @Async("WorkerPool")
    protected void unsubscribeFromSubjects(List<String> subjects, String agentID)
    {
        subjects.forEach(s ->
        {
            subscriptionsBySubject.computeIfPresent(s, (k, v) ->
            {
                v.remove(agentID);
                if (v.isEmpty())
                {
                    SubjectSubscriptionEvent subjectSubscriptionEvent = new SubjectSubscriptionEvent(this, s, SubscriptionEventType.UNSUBSCRIPTION);
                    eventPublisher.publishEvent(subjectSubscriptionEvent);
                }
                return v;
            });
        });
    }


    /**
     * Subscribes the agent to the MRN associated with their connection.
     * An event is published when the number of subscribers for an MRN goes from 0 to 1.
     *
     * @param agentID The agent ID to be subscribed.
     */
    @Async("WorkerPool")
    protected void subscribeToMrn(String agentID)
    {
        Optional<String> mrnOptional = connectionRepository.getMRN(agentID);

        if (mrnOptional.isPresent())
        {
            String mrn = mrnOptional.get();

            subscriptionsByMrn.computeIfAbsent(mrn, k ->
            {
                MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(this, mrn, SubscriptionEventType.SUBSCRIPTION);
                eventPublisher.publishEvent(mrnSubscriptionEvent);
                return new ConcurrentSkipListSet<>();
            }).add(agentID);
        }
        else
        {
            ConnectionCloseRequest request = new ConnectionCloseRequest(this, agentID, WebSocketCloseStatus.POLICY_VIOLATION.code(), "No PKIIdentity found for agentID");
            eventPublisher.publishEvent(request);
        }
    }


    /**
     * Unsubscribes the agent from the MRN associated with their connection.
     * An event is published when the number of subscribers for an MRN goes from 1 to 0.
     *
     * @param agentID The agent ID to be unsubscribed.
     */
    @Async("WorkerPool")
    protected void unsubscribeFromMrn(String agentID)
    {
        Optional<String> mrnOptional = connectionRepository.getMRN(agentID);

        if (mrnOptional.isPresent())
        {
            String mrn = mrnOptional.get();

            subscriptionsByMrn.computeIfPresent(mrn, (k, v) ->
            {
                v.remove(agentID);
                if (v.isEmpty())
                {
                    MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(this, mrn, SubscriptionEventType.UNSUBSCRIPTION);
                    eventPublisher.publishEvent(mrnSubscriptionEvent);
                }
                return v;
            });
        }
    }


    /**
     * This method is called during the destruction of the SubscriptionRepository.
     * It clears the subscriptionsBySubject and subscriptionsByMrn maps.
     */
    @PreDestroy
    public void destroy()
    {
        subscriptionsBySubject.clear();
        subscriptionsByMrn.clear();
    }
}
