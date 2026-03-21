package com.civic_reporting.cittilenz.service;

public interface WebSocketNotificationService {

    void pushNotification(Integer userId, Object payload);

}