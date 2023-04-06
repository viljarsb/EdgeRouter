package com.mms.EdgeRouter.MessageHandlers.Remote;


import com.mms.EdgeRouter.ActiveMQ.events.RemoteMessageEvent;


/**
 * Interface for services responsible for handling remote messages received from other brokers.
 */
public interface IRemoteMessageHandler
{
    /**
     * Handles a given {@link RemoteMessageEvent}.
     *
     * @param event The RemoteMessageEvent to handle.
     */
    void onRemoteMessage(RemoteMessageEvent event);
}
