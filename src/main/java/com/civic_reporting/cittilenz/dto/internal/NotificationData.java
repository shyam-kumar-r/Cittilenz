package com.civic_reporting.cittilenz.dto.internal;

import com.civic_reporting.cittilenz.entity.Issue;
import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.User;

public record NotificationData(

        Notification notification,

        User user,

        Issue issue,

        String officialName

) {
}