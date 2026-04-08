package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.UserDevice;
import com.civic_reporting.cittilenz.repository.UserDeviceRepository;
import com.civic_reporting.cittilenz.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationRouterServiceImpl implements NotificationRouterService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationRouterServiceImpl.class);

    private final EmailService emailService;
    private final WebSocketNotificationService websocket;
    private final PushNotificationService pushService;
    private final UserDeviceRepository deviceRepository;

    public NotificationRouterServiceImpl(
            EmailService emailService,
            WebSocketNotificationService websocket,
            PushNotificationService pushService,
            UserDeviceRepository deviceRepository) {

        this.emailService = emailService;
        this.websocket = websocket;
        this.pushService = pushService;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void route(Notification n) {

        String channel = n.getChannel();

        if (channel == null) {
            throw new IllegalArgumentException("Notification channel is required");
        }

        switch (channel) {

            case "EMAIL" -> {
                if (n.getEmail() == null) {
                    throw new IllegalArgumentException("Email missing");
                }
                emailService.sendEmail(n.getEmail(), n.getTitle(), n.getMessage());
            }

            case "IN_APP" -> {
                if (n.getUserId() == null) {
                    throw new IllegalArgumentException("UserId missing");
                }
                websocket.pushNotification(n.getUserId(), n);
            }

            case "PUSH" -> {
                if (n.getUserId() == null) {
                    throw new IllegalArgumentException("UserId missing");
                }

                List<UserDevice> devices =
                        deviceRepository.findByUserIdAndActiveTrue(n.getUserId());

                for (UserDevice device : devices) {
                    pushService.sendPush(
                            device.getDeviceToken(),
                            n.getTitle(),
                            n.getMessage()
                    );
                }
            }

            default -> throw new IllegalArgumentException("Unsupported channel: " + channel);
        }

        log.info("Notification routed | id={} channel={}", n.getId(), channel);
    }
}