package com.mms.EdgeRouter.MessageHandler;

import Protocols.MMTP.MessageFormats.DirectApplicationMessage;
import com.mms.EdgeRouter.AsyncTaskManagement.AsyncServiceExecutor;
import com.mms.EdgeRouter.AsyncTaskManagement.TaskPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class LocalMessageHandler
{
    private final AsyncServiceExecutor asyncServiceExecutor;

    @Autowired
    public LocalMessageHandler(AsyncServiceExecutor asyncServiceExecutor)
    {
        this.asyncServiceExecutor = asyncServiceExecutor;
    }


    public void handleLocalMessage(ByteBuffer buffer, String agentID)
    {
        asyncServiceExecutor.runAsync(() -> processLocalMessage(buffer, agentID), TaskPriority.HIGH);
    }


    private void processLocalMessage(ByteBuffer buffer, String agentID)
    {
        DirectApplicationMessage message = DirectApplicationMessage.parseFrom(buffer);
    }

}
