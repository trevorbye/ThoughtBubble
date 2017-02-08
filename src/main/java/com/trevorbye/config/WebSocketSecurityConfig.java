package com.trevorbye.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpSubscribeDestMatchers("/main-page-feed/thought-queue/**").permitAll()
                .simpMessageDestMatchers("/thought-bubble/push-to-queue/**").authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }


}


