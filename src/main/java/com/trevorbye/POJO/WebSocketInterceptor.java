package com.trevorbye.POJO;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketInterceptor extends ChannelInterceptorAdapter {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        Principal principal = headerAccessor.getUser();
        SimpMessageType messageType = headerAccessor.getCommand().getMessageType();

        if (!validAuthentication(principal) && messageType.equals(SimpMessageType.MESSAGE)) {
            throw new IllegalArgumentException("You must be logged in to send a thought.");
        } else {
            return message;
        }

    }

    private boolean validAuthentication(Principal principal) {
        return principal != null;
    }
}
