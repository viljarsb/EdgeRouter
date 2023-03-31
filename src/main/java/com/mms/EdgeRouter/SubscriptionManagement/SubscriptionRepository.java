package com.mms.EdgeRouter.SubscriptionManagement;

import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionAddedEvent;
import com.mms.EdgeRouter.ConnectionManagement.Events.ConnectionRemovedEvent;
import com.mms.EdgeRouter.ConnectionManagement.IConnectionRepository;
import com.mms.EdgeRouter.SubscriptionManagement.Events.MrnSubscriptionEvent;
import com.mms.EdgeRouter.SubscriptionManagement.Events.SubjectSubscriptionEvent;
import com.mms.EdgeRouter.SubscriptionManagement.Events.SubscriptionEventType;
import lombok.NonNull;
import net.maritimeconnectivity.pki.PKIIdentity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Repository
public class SubscriptionRepository implements ISubscriptionRepository
{
    private final Map<String, ConcurrentSkipListSet<String>> subscriptionsBySubject = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentSkipListSet<String>> subscriptionsByMrn = new ConcurrentHashMap<>();

    private final IConnectionRepository connectionRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Autowired
    public SubscriptionRepository(@NonNull IConnectionRepository connectionRepository, @NonNull ApplicationEventPublisher eventPublisher)
    {
        this.connectionRepository = connectionRepository;
        this.eventPublisher = eventPublisher;
    }


    @EventListener
    public void handleConnectionAddedEvent(ConnectionAddedEvent event)
    {
        String agentID = event.getAgentID();
        Optional<PKIIdentity> identityOptional = event.getIdentity();

        if (identityOptional.isPresent())
        {
            PKIIdentity identity = identityOptional.get();
            String mrn = identity.getMrn();
            subscriptionsByMrn.computeIfAbsent(mrn, k ->
            {
                MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(mrn, SubscriptionEventType.SUBSCRIPTION);
                eventPublisher.publishEvent(mrnSubscriptionEvent);
                return new ConcurrentSkipListSet<>();
            }).add(agentID);
        }
    }


    @EventListener
    public void handleConnectionRemovedEvent(ConnectionRemovedEvent event)
    {
        String agentID = event.getAgentID();

        // Remove agentID from subscriptionsBySubject map
        subscriptionsBySubject.compute(agentID, (key, set) ->
        {
            if (set != null)
            {
                set.remove(agentID);
                if (set.isEmpty())
                {
                    subscriptionsBySubject.remove(key);
                    SubjectSubscriptionEvent subjectSubscriptionEvent = new SubjectSubscriptionEvent(key, SubscriptionEventType.UNSUBSCRIPTION);
                    eventPublisher.publishEvent(subjectSubscriptionEvent);
                }
            }
            return set;
        });

        // Remove agentID from subscriptionsByMrn map
        Optional<PKIIdentity> identityOptional = connectionRepository.getIdentity(agentID);
        if (identityOptional.isPresent())
        {
            PKIIdentity identity = identityOptional.get();
            String mrn = identity.getMrn();
            subscriptionsByMrn.compute(mrn, (key, set) ->
            {
                if (set != null)
                {
                    set.remove(agentID);
                    if (set.isEmpty())
                    {
                        subscriptionsByMrn.remove(mrn);
                        // Publish event to notify subscribers of change
                        MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(mrn, SubscriptionEventType.UNSUBSCRIPTION);
                        eventPublisher.publishEvent(mrnSubscriptionEvent);
                    }
                }
                return set;
            });
        }
    }


    @Override
    public void subscribeToSubjects(List<String> subject, String agentID)
    {
        for (String s : subject)
        {
            subscriptionsBySubject.compute(s, (k, v) ->
            {
                if (v == null)
                {
                    v = new ConcurrentSkipListSet<>();
                    SubjectSubscriptionEvent subjectSubscriptionEvent = new SubjectSubscriptionEvent(s, SubscriptionEventType.SUBSCRIPTION);
                    eventPublisher.publishEvent(subjectSubscriptionEvent);
                }
                v.add(agentID);
                return v;
            });
        }
    }


    @Override
    public void unsubscribeFromSubjects(List<String> subjects, String agentID)
    {
        for (String s : subjects)
        {
            subscriptionsBySubject.computeIfPresent(s, (k, v) ->
            {
                v.remove(agentID);
                if (v.isEmpty())
                {
                    SubjectSubscriptionEvent subjectSubscriptionEvent = new SubjectSubscriptionEvent(s, SubscriptionEventType.UNSUBSCRIPTION);
                }
                return v;
            });
        }
    }


    @Override
    public void subscribeToMrn(String agentID)
    {
        Optional<PKIIdentity> identityOptional = connectionRepository.getIdentity(agentID);

        if (identityOptional.isPresent())
        {
            PKIIdentity identity = identityOptional.get();
            String mrn = identity.getMrn();

            subscriptionsByMrn.computeIfAbsent(mrn, k ->
            {
                MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(mrn, SubscriptionEventType.SUBSCRIPTION);
                eventPublisher.publishEvent(mrnSubscriptionEvent);
                return new ConcurrentSkipListSet<>();
            }).add(agentID);
        }
    }


    @Override
    public void unsubscribeFromMrn(String agentID)
    {
        Optional<PKIIdentity> identityOptional = connectionRepository.getIdentity(agentID);

        if (identityOptional.isPresent())
        {
            PKIIdentity identity = identityOptional.get();
            String mrn = identity.getMrn();

            subscriptionsByMrn.computeIfPresent(mrn, (k, v) ->
            {
                v.remove(agentID);
                if (v.isEmpty())
                {
                    MrnSubscriptionEvent mrnSubscriptionEvent = new MrnSubscriptionEvent(mrn, SubscriptionEventType.UNSUBSCRIPTION);
                    eventPublisher.publishEvent(mrnSubscriptionEvent);
                }
                return v;
            });
        }
    }


    @Override
    public Set<String> getSubscribersBySubject(String subject)
    {
        return new HashSet<>(subscriptionsBySubject.get(subject));
    }


    @Override
    public Set<String> getSubscribersBySubjects(List<String> subjects)
    {
        Set<String> subscribers = new HashSet<>();
        for (String subject : subjects)
        {
            subscribers.addAll(getSubscribersBySubject(subject));
        }

        return subscribers;
    }


    @Override
    public Set<String> getSubscribersByMrn(String mrn)
    {
        return new HashSet<>(subscriptionsByMrn.get(mrn));
    }


    @Override
    public Set<String> getSubscribersByMrns(List<String> mrns)
    {
        Set<String> subscribers = new HashSet<>();
        for (String mrn : mrns)
        {
            subscribers.addAll(getSubscribersByMrn(mrn));
        }

        return subscribers;
    }


    @Override
    public Map<String, String> getSubjectSubscriptionMap()
    {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, ConcurrentSkipListSet<String>> entry : subscriptionsBySubject.entrySet())
        {
            String subject = entry.getKey();
            for (String agentID : entry.getValue())
            {
                map.put(subject, agentID);
            }
        }

        return map;
    }


    @Override
    public Map<String, String> getMrnSubscriptionMap()
    {
        HashMap<String, String> map = new HashMap<>();
        for (Map.Entry<String, ConcurrentSkipListSet<String>> entry : subscriptionsByMrn.entrySet())
        {
            String mrn = entry.getKey();
            for (String agentID : entry.getValue())
            {
                map.put(mrn, agentID);
            }
        }

        return map;
    }
}
