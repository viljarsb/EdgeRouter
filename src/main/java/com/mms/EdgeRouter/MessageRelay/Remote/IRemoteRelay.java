package com.mms.EdgeRouter.MessageRelay.Remote;

import com.mms.EdgeRouter.MessageRelay.Events.RemoteDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.RemoteSubjectMessageForwardRequest;

/**
 * An interface for classes that handle remote message forwarding requests.
 */
public interface IRemoteRelay
{
    void onRemoteForwardingRequest(RemoteDirectMessageForwardRequest event);

    void onRemoteForwardingRequest(RemoteSubjectMessageForwardRequest event);
}
