package com.trevorbye.web;

import com.trevorbye.model.ThoughtEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.Calendar;

@RestController
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/push-to-queue")
    public void pushThoughtToQueue(ThoughtEntity entity) throws Exception {
        entity.setPostDate(new Date(Calendar.getInstance().getTimeInMillis()));
        this.simpMessagingTemplate.convertAndSend("/main-page-feed/thought-queue", entity);
    }
}
