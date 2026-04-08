package com.civic_reporting.cittilenz.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.NotificationPreference;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.exception.ResourceNotFoundException;
import com.civic_reporting.cittilenz.repository.NotificationPreferenceRepository;
import com.civic_reporting.cittilenz.repository.NotificationRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void notifyUser(Integer userId,
                           String title,
                           String message,
                           String type,
                           Integer issueId) {

        boolean emailEnabled = preferenceRepository
                .findByUserIdAndNotificationType(userId, type)
                .map(NotificationPreference::isEmailEnabled)
                .orElse(true);

        if (!emailEnabled) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification n = new Notification();

        n.setUserId(userId);
        n.setEmail(user.getEmail());
        n.setTitle(title);
        n.setMessage(message);
        n.setNotificationType(type);
        n.setIssueId(issueId);

        n.setChannel("EMAIL");
        n.setStatus("PENDING");
        n.setRetryCount(0);
        n.setCreatedAt(LocalDateTime.now());
        n.setActive(true);

        notificationRepository.save(n);
    }
}