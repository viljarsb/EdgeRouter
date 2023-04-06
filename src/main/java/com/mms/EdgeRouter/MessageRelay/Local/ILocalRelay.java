package com.mms.EdgeRouter.MessageRelay.Local;

import com.mms.EdgeRouter.MessageRelay.Events.LocalDirectMessageForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.LocalSubjectMessageForwardRequest;

/**
 * An interface for classes that handle local message forwarding requests.
 */
public interface ILocalRelay
{
    void onLocalForwardingRequest(LocalDirectMessageForwardRequest event);

    void onLocalForwardingRequest(LocalSubjectMessageForwardRequest event);
}