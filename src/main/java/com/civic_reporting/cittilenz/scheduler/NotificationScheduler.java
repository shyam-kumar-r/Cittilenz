package com.civic_reporting.cittilenz.scheduler;

import com.civic_reporting.cittilenz.service.NotificationProcessorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
     */
    @Scheduled(fixedDelay = 30000)
    public void processNotifications() {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        long start = System.currentTimeMillis();

        log.info("NotificationScheduler START | traceId={}", traceId);

        try {
            processor.processQueue();

            long duration = System.currentTimeMillis() - start;

            log.info("NotificationScheduler SUCCESS | traceId={} | duration={} ms",
                    traceId, duration);

        } catch (Exception ex) {

            long duration = System.currentTimeMillis() - start;

            log.error("NotificationScheduler FAILED | traceId={} | duration={} ms",
                    traceId, duration, ex);

        } finally {
            MDC.clear();
        }
    }
}