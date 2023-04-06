package com.mms.EdgeRouter.MessageHandlers.Local;

import com.mms.EdgeRouter.WebSocket.Events.LocalMessageEvent;

/**
 * Interface for handling local message events, that is messages received from the local clients.
 */
public interface ILocalMessageHandler
{
    /**
     * Handles a given {@link LocalMessageEvent}.
     *
     * @param event The LocalMessageEvent to handle.
     */
    void onLocalMessage(LocalMessageEvent event);
}
