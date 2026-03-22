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

    /**
     * Runs every 30 seconds
     * Processes notification queue safely
     */
    @Scheduled(fixedDelay = 30000)
    public void processNotifications() {

        long start = System.currentTimeMillis();

        log.info("Notification worker started");

        try {
            processor.processQueue();
        } catch (Exception ex) {
            log.error("Notification processing failed", ex);
        }

        long duration = System.currentTimeMillis() - start;

        log.info("Notification worker finished in {} ms", duration);
    }
}