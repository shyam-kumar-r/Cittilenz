package com.civic_reporting.cittilenz.service;

import com.civic_reporting.cittilenz.entity.Notification;

public interface NotificationRouterService {

    void route(Notification notification);

}