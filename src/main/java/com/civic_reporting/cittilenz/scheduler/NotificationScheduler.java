package com.civic_reporting.cittilenz.scheduler;

import com.civic_reporting.cittilenz.service.NotificationProcessorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationProcessorService processor;

    public NotificationScheduler(NotificationProcessorService processor) {
        this.processor = processor;
    }

    @Scheduled(fixedDelayString = "30000")
    public void processNotifications() {

        log.info("Notification worker started");

        processor.processQueue();

        log.info("Notification worker finished");
    }
}