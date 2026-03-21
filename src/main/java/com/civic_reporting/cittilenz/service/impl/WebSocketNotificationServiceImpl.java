package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.service.WebSocketNotificationService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationServiceImpl
        implements WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationServiceImpl(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void pushNotification(Integer userId, Object payload) {

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                payload
        );
    }
}