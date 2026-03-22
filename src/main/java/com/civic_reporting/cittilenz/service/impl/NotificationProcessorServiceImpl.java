package com.civic_reporting.cittilenz.service.impl;

import com.civic_reporting.cittilenz.entity.Notification;
import com.civic_reporting.cittilenz.entity.User;
import com.civic_reporting.cittilenz.repository.NotificationRepository;
import com.civic_reporting.cittilenz.repository.UserRepository;
import com.civic_reporting.cittilenz.service.EmailService;
import com.civic_reporting.cittilenz.service.NotificationProcessorService;
import com.civic_reporting.cittilenz.service.NotificationRouterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationProcessorServiceImpl implements NotificationProcessorService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationProcessorServiceImpl.class);

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;

    private final NotificationRepository notificationRepository;
    private final NotificationRouterService notificationRouterService;

    public NotificationProcessorServiceImpl(
            NotificationRepository notificationRepository,
            NotificationRouterService notificationRouterService) {

        this.notificationRepository = notificationRepository;
        this.notificationRouterService = notificationRouterService;
    }

    @Override
    @Transactional
    public void processQueue() {

        List<Notification> notifications =
                notificationRepository.fetchPendingForProcessing(BATCH_SIZE);

        if (notifications.isEmpty()) {
            return;
        }

        for (Notification notification : notifications) {

            try {

                processNotification(notification);

            } catch (Exception ex) {

                log.error(
                        "Notification processing failed for id {}",
                        notification.getId(),
                        ex
                );

                handleFailure(notification);
            }
        }
    }

    private void processNotification(Notification notification) {

        // 🔥 NO USER FETCH HERE

        notificationRouterService.route(notification);

        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notification.setLastAttemptAt(LocalDateTime.now());

        notificationRepository.save(notification);

        log.info("Notification sent successfully id={}", notification.getId());
    }

    private void handleFailure(Notification notification) {

        int retry = notification.getRetryCount() == null
                ? 1
                : notification.getRetryCount() + 1;

        notification.setRetryCount(retry);
        notification.setLastAttemptAt(LocalDateTime.now());

        if (retry >= MAX_RETRIES) {
            notification.setStatus("FAILED");
        } else {
            notification.setStatus("PENDING");
        }

        notificationRepository.save(notification);
    }
}