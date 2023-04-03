package com.mms.EdgeRouter.MessageHandler.Local;

import com.mms.EdgeRouter.WebSocket.Events.LocalMessageEvent;

/**
 * Interface for handling local messages, that is messages received from the local clients.
 */
public interface ILocalMessageHandler
{
    /**
     * Handles a LocalMessageEvent.
     *
     * @param event The LocalMessageEvent to handle.
     */
    void onLocalMessage(LocalMessageEvent event);
}
