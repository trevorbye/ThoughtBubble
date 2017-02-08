package com.trevorbye.config;

import com.trevorbye.POJO.WebSocketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //Javascript connection subscribes to this URI
        config.enableSimpleBroker("/main-page-feed");

        //STOMP messages are sent to this URI + suffix
        config.setApplicationDestinationPrefixes("/thought-bubble");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //URI used for SockJS connection
        registry.addEndpoint("/application-socket-conn").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new WebSocketInterceptor());
    }
}
