package com.mms.EdgeRouter.MessageRelay.Local.MessageTracker;

public interface IMessageTracker
{
    void registerSent(String messageId, String agentID);
    boolean checkRebound(String messageId, String agentID);
    void registerDelivery(String messageId, String agentID);
    boolean checkDeliveryStatus(String messageId, String agentID);
}
