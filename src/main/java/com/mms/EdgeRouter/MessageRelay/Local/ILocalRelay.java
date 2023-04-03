package com.mms.EdgeRouter.MessageRelay.Local;

import com.mms.EdgeRouter.MessageRelay.Events.LocalDirectForwardRequest;
import com.mms.EdgeRouter.MessageRelay.Events.LocalSubjectForwardRequest;

public interface ILocalRelay
{
    void onLocalForwardingRequest(LocalDirectForwardRequest event);
    void onLocalForwardingRequest(LocalSubjectForwardRequest event);
}