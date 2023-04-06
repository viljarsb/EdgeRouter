package com.mms.EdgeRouter.ServiceBroadcaster;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;


/**
 * This class is responsible for registering the edge router service with mDNS using JmDNS.
 * <p>
 * It creates a ServiceInfo object with the necessary information (service type, service name, port, and path),
 * and registers it with JmDNS.
 */
@Service
@Slf4j
public class mDNSService
{
    private final JmDNS jmDNS;
    private final static String SERVICE_TYPE = "_mms-edge-router._tcp.local.";

    @Value("${edgerouter.serviceName:mms-edge-router}")
    private String serviceName;
    @Value("${edgerouter.path:/ws}")
    private String path;
    @Value("${server.port}")
    private int port;


    /**
     * Constructs a new instance of the mDNSService class with the specified JmDNS instance.
     *
     * @param jmDNS The JmDNS instance to use for registering the service.
     */
    public mDNSService(JmDNS jmDNS)
    {
        this.jmDNS = jmDNS;
    }


    /**
     * Registers the edge router service with mDNS using the JmDNS instance.
     *
     * @throws IOException If an I/O error occurs while registering the service.
     */
    @PostConstruct
    @Async("WorkerPool")
    public void init() throws IOException
    {
        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, port, "path=" + path);
        jmDNS.registerService(serviceInfo);
        log.info("Registered edge router service={}, with mDNS", serviceInfo);
    }


    /**
     * Unregisters the edge router service from mDNS and closes the JmDNS instance.
     *
     * @throws IOException If an I/O error occurs while unregistering the service or closing the JmDNS instance.
     */
    @Async("WorkerPool")
    @PreDestroy
    public void close() throws IOException
    {
        log.info("Attempting to unregister edge router service from mDNS and close JmDNS instance.");
        jmDNS.unregisterAllServices();
        jmDNS.close();
        log.info("Successfully unregistered edge router service from mDNS and closed JmDNS instance.");
    }
}
