package com.mms.EdgeRouter.ServiceBroadcaster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jmdns.JmDNS;
import java.io.IOException;
import java.net.InetAddress;


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
    @Value("${server.port:8080}")
    private int port;
    @Value("${server.address:0.0.0.0}")
    private String address;


    /**
     * Creates a new instance of the JmDNS class with the server's address.
     *
     * @return A new instance of the JmDNS class with the server's address.
     * @throws IOException          If an I/O error occurs while creating the JmDNS instance.
     */
    @Bean
    JmDNS mDNS() throws IOException
    {
        log.info("Attempting to create JmDNS instance with address={}", address);
        InetAddress inetAddress = InetAddress.getByName(address);
        JmDNS jmDNS = JmDNS.create(inetAddress);
        log.info("Successfully JmDNS instance with address={}", address);
        return jmDNS;
    }
}
