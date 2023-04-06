package com.mms.EdgeRouter.ActiveMQ;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;


/**
 * The `RemoteConfig` class is a configuration class for ActiveMQ. It sets up the connection to the ActiveMQ broker,
 * the `JmsTemplate` used to send messages to the broker, and the `DefaultMessageListenerContainer` used to listen for
 * messages from the broker.
 */
@EnableJms
@Configuration
public class RemoteConfig
{
    @Value("${mms.remote.broker:#{null}}")
    private String brokerUrl;

    @Value("${spring.artemis.user:admin}")
    private String username;

    @Value("${spring.artemis.password:admin}")
    private String password;

    @Value("${activemq.ssl.trustStore:#{null}}")
    private String trustStore;

    @Value("${activemq.ssl.trustStorePassword:#{null}}")
    private String trustStorePassword;

    @Value("${activemq.ssl.keyStore:#{null}}")
    private String keyStore;

    @Value("${activemq.ssl.keyStorePassword:#{null}}")
    private String keyStorePassword;


    /**
     * Creates and returns an `ActiveMQConnectionFactory` object for the ActiveMQ broker with the specified URL,
     * username, and password.
     *
     * @return An `ActiveMQConnectionFactory` object for the ActiveMQ broker.
     */
    @Bean
    public ActiveMQConnectionFactory connectionFactory()
    {
        return new ActiveMQConnectionFactory(brokerUrl, username, password);
    }


    /**
     * Creates and returns a `CachingConnectionFactory` object that caches the `ActiveMQConnectionFactory`
     * for improved performance.
     *
     * @return A `CachingConnectionFactory` object for the ActiveMQ broker.
     */
    @Bean(name = "activeMQConnectionFactory")
    public CachingConnectionFactory cachingConnectionFactory()
    {
        return new CachingConnectionFactory(connectionFactory());
    }


    /**
     * Creates and returns a `JmsTemplate` object that uses the `CachingConnectionFactory` to send messages to the
     * ActiveMQ broker.
     *
     * @return A `JmsTemplate` object for the ActiveMQ broker.
     */
    @Bean
    public JmsTemplate jmsTemplate()
    {
        JmsTemplate template = new JmsTemplate(cachingConnectionFactory());
        template.setPubSubDomain(true);
        return template;
    }
}