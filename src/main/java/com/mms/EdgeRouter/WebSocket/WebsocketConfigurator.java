package com.mms.EdgeRouter.WebSocket;

import com.mms.EdgeRouter.ConnectionManagement.ConnectionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebsocketConfigurator implements WebSocketConfigurer
{
    @Value("${websocket.port:8080}")
    private int port;
    @Value("${websocket.host:localhost}")
    private String host;
    @Value("${websocket.path:/edgerouter/websocket}")
    private String path;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(webSocketHandler(), "/websocket");
    }


    @Bean
    public WebSocketHandler webSocketHandler()
    {
        return new WebSocketHandler();
    }


    @Bean
    public ConnectionRepository connectionEventManagement()
    {
        return new ConnectionRepository();
    }
}
