package com.mms.EdgeRouter.MessageHandler;

import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public interface ILocalMessageHandler
{
    void handleLocalMessage(ByteBuffer buffer, String agentID);
}
