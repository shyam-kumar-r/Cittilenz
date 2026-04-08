package com.civic_reporting.cittilenz.scheduler;

import com.civic_reporting.cittilenz.service.SlaService;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SlaScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(SlaScheduler.class);

    private final SlaService slaService;

    public SlaScheduler(SlaService slaService) {
        this.slaService = slaService;
    }

    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(
            name = "slaProcessorLock",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT2M"
    )
    public void runSlaProcessor() {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        long start = System.currentTimeMillis();

        log.info("SlaScheduler START | traceId={}", traceId);

        try {
            slaService.processSlaBreaches();

            long duration = System.currentTimeMillis() - start;

            log.info("SlaScheduler SUCCESS | traceId={} | duration={} ms",
                    traceId, duration);

        } catch (Exception ex) {

            long duration = System.currentTimeMillis() - start;

            log.error("SlaScheduler FAILED | traceId={} | duration={} ms",
                    traceId, duration, ex);

        } finally {
            MDC.clear();
        }
    }

    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(
            name = "reassignmentProcessorLock",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT2M"
    )
    public void processReassignedIssues() {

        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        long start = System.currentTimeMillis();

        log.info("ReassignmentScheduler START | traceId={}", traceId);

        try {
            slaService.processReassignedIssues();

            long duration = System.currentTimeMillis() - start;

            log.info("ReassignmentScheduler SUCCESS | traceId={} | duration={} ms",
                    traceId, duration);

        } catch (Exception ex) {

            long duration = System.currentTimeMillis() - start;

            log.error("ReassignmentScheduler FAILED | traceId={} | duration={} ms",
                    traceId, duration, ex);

        } finally {
            MDC.clear();
        }
    }
}