package com.mms.EdgeRouter.SubscriptionManagement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISubscriptionRepository
{
    void subscribeToSubjects(List<String> subject, String agentID);
    void unsubscribeFromSubjects(List<String> subjects, String agentID);
    void subscribeToMrn(String agentID);
    void unsubscribeFromMrn(String agentID);
    Set<String> getSubscribersBySubject(String subject);
    Set<String> getSubscribersBySubjects(List<String> subjects);
    Set<String> getSubscribersByMrn(String mrn);
    Set<String> getSubscribersByMrns(List<String> mrns);
    Map<String, String> getSubjectSubscriptionMap();
    Map<String, String> getMrnSubscriptionMap();
}
