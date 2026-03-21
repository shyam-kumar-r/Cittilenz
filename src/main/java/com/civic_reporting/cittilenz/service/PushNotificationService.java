package com.civic_reporting.cittilenz.service;

public interface PushNotificationService {

    void sendPush(String deviceToken,
                  String title,
                  String message);

}