package com.civic_reporting.cittilenz.scheduler;

import com.civic_reporting.cittilenz.service.SlaService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

        long start = System.currentTimeMillis();

        log.info("SLA Scheduler started");

        try {
            // 🔥 THIS inserts notifications into DB
            slaService.processSlaBreaches();

        } catch (Exception ex) {
            log.error("SLA Scheduler failed", ex);
        }

        long duration = System.currentTimeMillis() - start;

        log.info("SLA Scheduler finished in {} ms", duration);
    }

    @Scheduled(fixedRate = 60000)
    public void processReassignedIssues() {

        try {
            slaService.processReassignedIssues();
        } catch (Exception ex) {
            log.error("Reassignment processing failed", ex);
        }
    }
}