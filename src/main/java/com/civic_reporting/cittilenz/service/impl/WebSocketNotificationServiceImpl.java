package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.service.WebSocketNotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationServiceImpl
        implements WebSocketNotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(WebSocketNotificationServiceImpl.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationServiceImpl(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void pushNotification(Integer userId, Object payload) {

        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    payload
            );

            log.info("WebSocket notification sent | userId={}", userId);

        } catch (Exception ex) {
            log.error("WebSocket push failed | userId={}", userId, ex);
        }
    }
}