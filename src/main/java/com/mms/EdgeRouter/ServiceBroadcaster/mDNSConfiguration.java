package com.mms.EdgeRouter.ServiceBroadcaster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * This class provides configuration for mDNS using JmDNS.
 * <p>
 * It creates a JmDNS instance and sets the address to the server's address (specified in the application.properties file).
 */
@Configuration
@Slf4j
public class mDNSConfiguration
{
    @Value("${edgerouter.serviceName:mms-edge-router}")
    private String serviceName;
    @Value("${edgerouter.path:/ws}")
    private String path;
    @Value("${server.port}")
    private int port;
    @Value("${server.address}")
    private String address;


    /**
     * Creates a new instance of the JmDNS class with the server's address.
     *
     * @return A new instance of the JmDNS class with the server's address.
     * @throws IOException          If an I/O error occurs while creating the JmDNS instance.
     * @throws InterruptedException If the thread is interrupted while waiting for the JmDNS instance to initialize.
     */
    @Bean
    JmDNS mDNS() throws IOException, InterruptedException
    {
        InetAddress inetAddress = InetAddress.getByName(address);
        return JmDNS.create(inetAddress);
    }
}
