package com.mms.EdgeRouter.MessageHandler.Remote;

import com.mms.EdgeRouter.RemoteMessageEvent;

/**
 * Interface for services responsible for handling remote messages received from other brokers.
 */
public interface IRemoteMessageHandler
{
    /**
     * Handles a RemoteMessageEvent.
     *
     * @param event The RemoteMessageEvent to handle.
     */
    void onRemoteMessage(RemoteMessageEvent event);
}
