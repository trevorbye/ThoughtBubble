package com.trevorbye.web;

import com.trevorbye.model.ThoughtEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/push-to-queue")
    public void pushThoughtToQueue(@Payload ThoughtEntity entity) throws Exception {
        this.simpMessagingTemplate.convertAndSend("/main-page-feed/thought-queue", entity);
    }
}
