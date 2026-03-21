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
    private final UserRepository userRepository;
    private final UserDeviceRepository deviceRepository;

    public NotificationRouterServiceImpl(
            EmailService emailService,
            WebSocketNotificationService websocket,
            PushNotificationService pushService,
            UserRepository userRepository,
            UserDeviceRepository deviceRepository) {

        this.emailService = emailService;
        this.websocket = websocket;
        this.pushService = pushService;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void route(Notification notification) {

        User user = userRepository
                .findById(notification.getUserId())
                .orElseThrow();

        switch (notification.getChannel()) {

            case "EMAIL" ->

                    emailService.sendEmail(
                            user.getEmail(),
                            notification.getTitle(),
                            notification.getMessage()
                    );

            case "IN_APP" ->

                    websocket.pushNotification(
                            user.getId(),
                            notification
                    );

            case "PUSH" -> {

                List<UserDevice> devices =
                        deviceRepository.findByUserIdAndActiveTrue(
                                user.getId()
                        );

                for (UserDevice device : devices) {

                    pushService.sendPush(
                            device.getDeviceToken(),
                            notification.getTitle(),
                            notification.getMessage()
                    );
                }
            }
        }
    }
}