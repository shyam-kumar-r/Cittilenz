package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.service.PushNotificationService;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import org.springframework.stereotype.Service;

@Service
public class PushNotificationServiceImpl
        implements PushNotificationService {

    @Override
    public void sendPush(String deviceToken,
                         String title,
                         String message) {

        try {

            Message msg = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(message)
                                    .build()
                    )
                    .build();

            FirebaseMessaging.getInstance().send(msg);

        } catch (Exception e) {

        	throw new RuntimeException("Push failed for token=" + deviceToken, e);
        }
    }
}