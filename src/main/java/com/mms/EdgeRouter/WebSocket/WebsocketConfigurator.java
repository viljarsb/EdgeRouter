package com.mms.EdgeRouter.WebSocket;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;


/**
 * The `WebsocketConfigurator` class is a configuration class for Spring's WebSocket support. It configures the WebSocket
 * endpoint and the server container to use for WebSocket connections, as well as the maximum message size, maximum idle
 * timeout, and other properties of the WebSocket connection.
 */
@Configuration
@EnableWebSocket
@Slf4j
public class WebsocketConfigurator implements WebSocketConfigurer
{
    private final RateLimiterWsHandler wsHandler;

    @Value("${edgerouter.path:/ws}")
    private String path;
    @Value("${edgerouter.maxMessageSize:8192}")
    private int maxMessageSize;
    @Value("${edgerouter.maxSessionIdleTimeout:700000}")
    private long maxSessionIdleTimeout;
    @Value("${edgerouter.asyncSendTimeout:1000}")
    private long asyncSendTimeout;


    /**
     * Constructs a new `{@link WebsocketConfigurator}` with the given `{@link WsHandler}`.
     *
     * @param WsHandler The `WsHandler` responsible for managing WebSocket connections.
     */
    @Autowired
    public WebsocketConfigurator(RateLimiterWsHandler wsHandler)
    {
        this.wsHandler = wsHandler;
    }


    /**
     * Registers the `{@link WsHandler}` with the `WebSocketHandlerRegistry`.
     *
     * @param registry The `WebSocketHandlerRegistry` used to register the `WsHandler`.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(wsHandler, path).setAllowedOrigins("*").addInterceptors(new ClientCertificateInterceptor());
    }


    /**
     * Creates and returns a `{@link ServletServerContainerFactoryBean}` object that configures the WebSocket server container.
     *
     * @return A `ServletServerContainerFactoryBean` object that configures the WebSocket server container.
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer()
    {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(100000); //FOR TESTING, SET TO 0 //TODO
        container.setMaxBinaryMessageBufferSize(maxMessageSize);
        container.setMaxSessionIdleTimeout(maxSessionIdleTimeout);
        container.setAsyncSendTimeout(asyncSendTimeout);
        return container;
    }
}
