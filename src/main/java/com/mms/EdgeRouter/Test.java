package com.mms.EdgeRouter;

import com.mms.EdgeRouter.ActiveMQ.RemoteSubscriber;
import com.mms.EdgeRouter.MessageRelay.Remote.RemoteRelay;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

@Service
public class Test
{
    private final RemoteSubscriber remoteSubscriber;
    private final RemoteRelay remoteRelay;


    @Autowired
    public Test(RemoteSubscriber remoteSubscriber, RemoteRelay remoteRelay)
    {
        this.remoteSubscriber = remoteSubscriber;
        this.remoteRelay = remoteRelay;
    }


    @PostConstruct
    public void init() throws ExecutionException
    {
        subscribe("test");

        transmit("test", ByteBuffer.wrap("Hello World!".getBytes()));
    }


    public void subscribe(String topicName)
    {
        remoteSubscriber.subscribe(topicName);
    }


    public void unsubscribe(String topicName)
    {
        remoteSubscriber.unsubscribe(topicName);
    }


    public void transmit(String destination, ByteBuffer payload) throws ExecutionException
    {
        remoteRelay.sendBytes(destination, payload);
    }
}