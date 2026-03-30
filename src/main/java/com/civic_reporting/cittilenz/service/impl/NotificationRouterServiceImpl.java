package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.UserDevice;

import com.civic_reporting.cittilenz.repository.UserDeviceRepository;

import com.civic_reporting.cittilenz.service.EmailService;
import com.civic_reporting.cittilenz.service.PushNotificationService;
import com.civic_reporting.cittilenz.service.WebSocketNotificationService;
import com.civic_reporting.cittilenz.service.NotificationRouterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationRouterServiceImpl
        implements NotificationRouterService {

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
    public void route(Notification notification) {

        String channel = notification.getChannel();

        if (channel == null) {
            throw new IllegalStateException("Notification channel is null");
        }

        switch (channel) {

            // ======================================
            // EMAIL
            // ======================================
            case "EMAIL" -> {

                if (notification.getEmail() == null) {
                    log.error("Email missing for notification id={}",
                            notification.getId());
                    throw new IllegalStateException("Email missing");
                }

                emailService.sendEmail(
                        notification.getEmail(),
                        notification.getTitle(),
                        notification.getMessage()
                );
            }

            // ======================================
            // IN-APP
            // ======================================
            case "IN_APP" -> {

                if (notification.getUserId() == null) {
                    log.error("UserId missing for IN_APP id={}",
                            notification.getId());
                    throw new IllegalStateException("UserId missing");
                }

                websocket.pushNotification(
                        notification.getUserId(),
                        notification
                );
            }

            // ======================================
            // PUSH
            // ======================================
            case "PUSH" -> {

                if (notification.getUserId() == null) {
                    log.error("UserId missing for PUSH id={}",
                            notification.getId());
                    throw new IllegalStateException("UserId missing");
                }

                List<UserDevice> devices =
                        deviceRepository.findByUserIdAndActiveTrue(
                                notification.getUserId()
                        );

                for (UserDevice device : devices) {

                    pushService.sendPush(
                            device.getDeviceToken(),
                            notification.getTitle(),
                            notification.getMessage()
                    );
                }
            }

            default -> throw new IllegalStateException(
                    "Unsupported channel: " + channel
            );
        }

        log.info("Notification routed id={} channel={}",
                notification.getId(), channel);
    }
}