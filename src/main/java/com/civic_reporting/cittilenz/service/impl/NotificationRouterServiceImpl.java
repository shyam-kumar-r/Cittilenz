package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.entity.UserDevice;

import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.repository.UserDeviceRepository;

import com.civic_reporting.cittilenz.service.EmailService;
import com.civic_reporting.cittilenz.service.PushNotificationService;
import com.civic_reporting.cittilenz.service.WebSocketNotificationService;
import com.civic_reporting.cittilenz.service.NotificationRouterService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationRouterServiceImpl
        implements NotificationRouterService {

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

        switch (notification.getChannel()) {

            // ======================================
            // EMAIL (DECOUPLED — NO USER FETCH)
            // ======================================
            case "EMAIL" -> {

                if (notification.getEmail() == null) {
                    throw new IllegalStateException(
                            "Email not present in notification"
                    );
                }

                emailService.sendEmail(
                        notification.getEmail(),
                        notification.getTitle(),
                        notification.getMessage()
                );
            }

            // ======================================
            // IN-APP (NEEDS USER)
            // ======================================
            case "IN_APP" -> {

                if (notification.getUserId() == null) {
                    throw new IllegalStateException(
                            "UserId required for IN_APP notification"
                    );
                }

                websocket.pushNotification(
                        notification.getUserId(),
                        notification
                );
            }

            // ======================================
            // PUSH (NEEDS USER DEVICES)
            // ======================================
            case "PUSH" -> {

                if (notification.getUserId() == null) {
                    throw new IllegalStateException(
                            "UserId required for PUSH notification"
                    );
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
                    "Unsupported notification channel: "
                            + notification.getChannel()
            );
        }
    }
}