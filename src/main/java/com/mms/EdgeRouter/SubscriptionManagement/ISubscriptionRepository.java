package com.mms.EdgeRouter.SubscriptionManagement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for managing subscriptions to subjects and MRNs. Subscriptions can be added and removed
 * for individual subjects and MRNs, and clients can query for the current set of subscribers to a given
 * subject or MRN.
 */
public interface ISubscriptionRepository
{
    List<String> getSubscribersBySubject(String subject);
    List<String> getSubscribersBySubjects(List<String> subjects);
    List<String> getSubscribersByMrn(String mrn);
    List<String> getSubscribersByMrns(List<String> mrns);
    Map<String, List<String>> getSubjectSubscriptionMap();
    Map<String, List<String>> getMrnSubscriptionMap();
}
